package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.services.TopicService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class VBrokerServerHandler extends SimpleChannelInboundHandler<VRequest> {

    private final TopicService topicService;
    ;
    private final RequestHandlerFactory requestHandlerFactory;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, VRequest request) {
        log.info("== ChannelRead0 ==");
        log.info("== Received VRequest {} with correlationId {} and type {} ==", request, request.correlationId(),
                request.requestMessageType());

        RequestHandler requestHandler = requestHandlerFactory.getRequestHandler(request, ctx);
        requestHandler.handle();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception caught in server handling", cause);
        ctx.close();
    }
}
