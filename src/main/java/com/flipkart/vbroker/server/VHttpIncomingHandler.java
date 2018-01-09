package com.flipkart.vbroker.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VHttpIncomingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private Channel outboundChannel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        log.info("Received FullHttpRequest ==");

        if (outboundChannel == null) {
            outboundChannel = getOutboundChannel(ctx);
        }
        outboundChannel.writeAndFlush(msg);
    }

    private Channel getOutboundChannel(ChannelHandlerContext ctx) {
        Bootstrap bootstrap = new Bootstrap()
                .group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new VBrokerServerResponseHandler());
                    }
                });

        ChannelFuture channelFuture = bootstrap.remoteAddress("localhost", 12000).connect();
        return channelFuture.channel();
    }
}
