package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.ResponseMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.entities.VResponse;
import com.flipkart.vbroker.protocol.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletionStage;

@Slf4j
@AllArgsConstructor
public class VBrokerRequestHandler extends SimpleChannelInboundHandler<VRequest> {

    private final RequestHandlerFactory requestHandlerFactory;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, VRequest vRequest) {
        log.info("== Received VRequest with correlationId {} and type {} ==", vRequest.correlationId(), vRequest.requestMessageType());
        RequestHandler requestHandler = requestHandlerFactory.getRequestHandler(vRequest);

        CompletionStage<VResponse> vResponseFuture = requestHandler.handle(vRequest);
        vResponseFuture.thenAccept(vResponse -> {
            ByteBuf responseByteBuf = Unpooled.wrappedBuffer(vResponse.getByteBuffer());
            Response response = new Response(responseByteBuf.readableBytes(), responseByteBuf);

            ChannelFuture channelFuture = ctx.writeAndFlush(response);
            if (vResponse.responseMessageType() == ResponseMessage.ProduceResponse) {
                //for now, we want to close the channel for produce request when response is written
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            }
        });
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
