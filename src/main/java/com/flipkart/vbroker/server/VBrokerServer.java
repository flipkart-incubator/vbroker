package com.flipkart.vbroker.server;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.VBrokerResponseHandler;
import com.flipkart.vbroker.ioengine.MessageService;
import com.flipkart.vbroker.protocol.codecs.VBrokerClientCodec;
import com.flipkart.vbroker.services.*;
import com.flipkart.vbroker.utils.DummyEntities;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class VBrokerServer implements Runnable {

    private final VBrokerConfig config;
    private final CountDownLatch mainLatch = new CountDownLatch(1);
    private Channel serverChannel;
    private Channel serverLocalChannel;

    public VBrokerServer(VBrokerConfig config) {
        this.config = config;
    }

    private void start() {
        Thread.currentThread().setName("vbroker_server");

        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("server_boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("server_worker"));
        EventLoopGroup localGroup = new DefaultEventLoopGroup(1, new DefaultThreadFactory("server_local"));

        TopicService topicService = new TopicServiceImpl();
        SubscriptionService subscriptionService = new SubscriptionServiceImpl();
        MessageService messageService = new MessageService();

        topicService.createTopic(DummyEntities.topic1);
        subscriptionService.createSubscription(DummyEntities.subscription1);

        ProducerService producerService = new ProducerService(messageService);
        RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory(
                producerService, topicService, subscriptionService);

        CountDownLatch latch = new CountDownLatch(2);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new VBrokerServerInitializer(requestHandlerFactory));

            ExecutorService remoteServerExecutor = Executors.newSingleThreadExecutor();
            remoteServerExecutor.submit(() -> {
                try {
                    serverChannel = serverBootstrap.bind(config.getBrokerHost(), config.getBrokerPort()).sync().channel();
                    log.info("Broker now listening on port {}", config.getBrokerPort());

                    serverChannel.closeFuture().sync();
                    latch.countDown();
                } catch (InterruptedException e) {
                    log.error("Exception in channel sync", e);
                }
            });

            LocalAddress address = new LocalAddress(new Random().nextInt(60000) + "");
            setupLocalSubscribers(localGroup, workerGroup, address, subscriptionService);

            //below used for local channel by the consumer
            ServerBootstrap serverLocalBootstrap = new ServerBootstrap();
            serverLocalBootstrap.group(localGroup, localGroup)
                    .channel(LocalServerChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new VBrokerServerInitializer(requestHandlerFactory));

            ExecutorService localServerExecutor = Executors.newSingleThreadExecutor();
            localServerExecutor.submit(() -> {
                try {
                    serverLocalChannel = serverLocalBootstrap.bind(address).sync().channel();
                    log.info("Consumer now listening on address {}", address);

                    serverLocalChannel.closeFuture().sync();
                    log.info("Consumer serverLocalChannel closed");
                    latch.countDown();
                } catch (InterruptedException e) {
                    log.error("Exception in channel sync", e);
                }
            });
            log.debug("Awaiting on latch");
            latch.await();

            mainLatch.countDown();
            log.debug("Latch countdown complete");
        } catch (InterruptedException e) {
            log.error("Exception in binding to/closing a channel", e);
        } finally {
            localGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() throws InterruptedException {
        if (serverChannel != null) {
            log.info("Closing serverChannel");
            serverChannel.close();
        }

        if (serverLocalChannel != null) {
            log.info("Closing serverLocalChannel");
            serverLocalChannel.close();
        }

        log.info("Waiting for servers to shutdown peacefully");
        mainLatch.await();
    }

    private void setupLocalSubscribers(EventLoopGroup localGroup,
                                       EventLoopGroup workerGroup,
                                       LocalAddress address,
                                       SubscriptionService subscriptionService) throws InterruptedException {
        Bootstrap httpClientBootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(1024 * 1024)); //1MB max
                        //pipeline.addLast(new VResponseEncoder());
                        pipeline.addLast(new HttpResponseHandler());
                    }
                });

        ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory(httpClientBootstrap);

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
                                pipeline.addLast(new VBrokerResponseHandler(responseHandlerFactory));
                            }
                        });
                    }
                });

        ExecutorService executorService = Executors.newSingleThreadExecutor(new DefaultThreadFactory("subscriber"));
        SubscriberDaemon subscriber = new SubscriberDaemon(address, consumerBootstrap, subscriptionService);
        log.info("Submitting SubscriberDaemon");
        executorService.submit(subscriber);
    }

    @Override
    public void run() {
        start();
    }
}
