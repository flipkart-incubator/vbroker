package com.flipkart.vbroker.client;

import com.flipkart.vbroker.protocol.VResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VBrokerClientHandler extends SimpleChannelInboundHandler<VResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, VResponse msg) throws Exception {
        log.info("Received VResponse from server {}", msg);
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception in client handling", cause);
        ctx.close();
    }
}
