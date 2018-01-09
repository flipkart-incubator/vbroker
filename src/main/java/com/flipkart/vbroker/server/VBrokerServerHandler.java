package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.VRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class VBrokerServerHandler extends SimpleChannelInboundHandler<VRequest> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, VRequest request) throws Exception {
        log.info("== ChannelRead0 ==");
        log.info("== Received VRequest {} with correlationId {} and type {} ==", request, request.correlationId(), request.requestMessageType());

        RequestHandler requestHandler = new RequestHandlerFactory(ctx).getRequestHandler(request);
        requestHandler.handle();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception caught in server handling", cause);
        ctx.close();
    }
}
