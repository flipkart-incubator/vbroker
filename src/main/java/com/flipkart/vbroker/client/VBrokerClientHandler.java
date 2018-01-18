package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.VResponse;
import com.flipkart.vbroker.server.ResponseHandler;
import com.flipkart.vbroker.server.ResponseHandlerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class VBrokerClientHandler extends SimpleChannelInboundHandler<VResponse> {

    private final ResponseHandlerFactory responseHandlerFactory;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, VResponse msg) {
        log.info("Received VResponse from server {}", msg);

        ResponseHandler responseHandler = responseHandlerFactory.getResponseHandler(msg, ctx);
        responseHandler.handle();

        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in client handling", cause);
        ctx.close();
    }
}
