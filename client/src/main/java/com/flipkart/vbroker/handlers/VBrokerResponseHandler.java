package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.VResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class VBrokerResponseHandler extends SimpleChannelInboundHandler<VResponse> {

    private final ResponseHandlerFactory responseHandlerFactory;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, VResponse vResponse) {
        log.info("== Received VResponse from server with correlationId {} and type {} ==",
            vResponse.correlationId(), vResponse.responseMessageType());

        ResponseHandler responseHandler = responseHandlerFactory.getResponseHandler(vResponse);
        responseHandler.handle(vResponse);
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
