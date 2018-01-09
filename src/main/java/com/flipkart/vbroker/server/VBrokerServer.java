package com.flipkart.vbroker.server;

import com.flipkart.vbroker.VBrokerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VBrokerServer {

    private final VBrokerConfig config;

    public VBrokerServer(VBrokerConfig config) {
        this.config = config;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new VBrokerServerInitializer());

//            Bootstrap bootstrap = new Bootstrap();
//            bootstrap.group(bossGroup)
//                    .channel(NioSocketChannel.class)
//                    .handler(new VBrokerServerInitializer());

            Channel channel = bootstrap.bind(config.getBrokerPort()).sync().channel();
            log.info("Broker now listening on port {}", config.getBrokerPort());
//
//            HttpRequest httpRequest = new DefaultFullHttpRequest(
//                    HttpVersion.HTTP_1_1,
//                    io.netty.handler.codec.http.HttpMethod.POST,
//                    "http://localhost:12000/messages",
//                    Unpooled.wrappedBuffer("{}".getBytes()));
//            httpRequest.headers().set(MessageConstants.MESSAGE_ID_HEADER, "msg-123");
//            httpRequest.headers().set(MessageConstants.GROUP_ID_HEADER, "group-123");
//
//            log.info("Making httpRequest to httpUri: {} and httpMethod: {}",
//                    httpRequest.uri(),
//                    httpRequest.method());
//            channel.writeAndFlush(httpRequest);

            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Exception in binding to/closing a channel", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
