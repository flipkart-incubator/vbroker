package com.flipkart.vbroker.server;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.VBrokerClientHandler;
import com.flipkart.vbroker.entities.FetchRequest;
import com.flipkart.vbroker.entities.RequestMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.protocol.codecs.VBrokerClientCodec;
import com.flipkart.vbroker.protocol.codecs.VBrokerServerCodec;
import com.flipkart.vbroker.services.ProducerService;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class VBrokerServer {

    private final VBrokerConfig config;

    public VBrokerServer(VBrokerConfig config) {
        this.config = config;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        EventLoopGroup localGroup = new DefaultEventLoopGroup(1);

        ProducerService producerService = new ProducerService();
        RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory(producerService);
        ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new VBrokerServerInitializer(requestHandlerFactory));

            log.info("Creating serverLocalBootstrap");
            //below used for local channel by the consumer
            ServerBootstrap serverLocalBootstrap = new ServerBootstrap();
            serverLocalBootstrap.group(localGroup)
                    .channel(LocalServerChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new VBrokerServerCodec());
                            pipeline.addLast(new ChannelInitializer<LocalChannel>() {
                                @Override
                                protected void initChannel(LocalChannel ch) throws Exception {
                                    ChannelPipeline pipeline = ch.pipeline();
                                    pipeline.addLast(new VBrokerServerCodec());
                                    pipeline.addLast(new VBrokerServerHandler(requestHandlerFactory));
                                }
                            });
                        }
                    });

            LocalAddress address = new LocalAddress(config.getConsumerPort() + "");
            log.info("Binding consumer to port {}", config.getConsumerPort());
            Channel serverLocalChannel = serverLocalBootstrap.bind(address).sync().channel();
            log.info("Consumer now listening on port {}", config.getConsumerPort());

//            Bootstrap clientBootstrap = new Bootstrap()
//                    .group(new NioEventLoopGroup(1))
//                    .channel(NioSocketChannel.class)
//                    .handler(new ChannelInitializer<Channel>() {
//                        @Override
//                        protected void initChannel(Channel ch) {
//                            ChannelPipeline pipeline = ch.pipeline();
//                            pipeline.addLast(new HttpClientCodec());
//                            pipeline.addLast(new VResponseEncoder());
//                            pipeline.addLast(new HttpResponseHandler());
//                        }
//                    });
//            MessageProcessor messageProcessor = new HttpMessageProcessor(clientBootstrap);

            Bootstrap consumerBootstrap = new Bootstrap()
                    .group(localGroup)
                    .channel(LocalChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ChannelInitializer<Channel>() {
                                @Override
                                protected void initChannel(Channel ch) {
                                    pipeline.addLast(new VBrokerClientCodec());
                                    pipeline.addLast(new VBrokerClientHandler(responseHandlerFactory));
                                }
                            });
                        }
                    });

            Thread consumerThread = new Thread(() -> {
                while (true) {
                    try {
                        Channel consumerChannel = consumerBootstrap.connect(address).sync().channel();

                        FlatBufferBuilder builder = new FlatBufferBuilder();
                        int fetchRequest = FetchRequest.createFetchRequest(builder,
                                (short) 11,
                                (short) 1,
                                (short) 1);
                        int vRequest = VRequest.createVRequest(builder,
                                (byte) 1,
                                1001,
                                RequestMessage.FetchRequest,
                                fetchRequest);
                        builder.finish(vRequest);
                        ByteBuffer byteBuffer = builder.dataBuffer();
                        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
                        Request request = new Request(byteBuf.readableBytes(), byteBuf);

                        log.info("Sending FetchRequest to broker");
                        consumerChannel.writeAndFlush(request);

                        break;
                    } catch (InterruptedException e) {
                        log.error("Exception in consumer in connecting to the broker", e);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            });
            consumerThread.start();

            serverLocalChannel.closeFuture().sync();

//            //start server where requests are accepted
//            log.info("Binding server to port {}", config.getBrokerPort());
//            Channel channel = serverBootstrap.bind(config.getBrokerHost(), config.getBrokerPort()).sync().channel();
//            log.info("Broker now listening on port {}", config.getBrokerPort());
//            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Exception in binding to/closing a channel", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
