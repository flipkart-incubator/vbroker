package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.ResponseMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.entities.VResponse;
import com.flipkart.vbroker.protocol.Response;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

@Slf4j
@AllArgsConstructor
public class VBrokerRequestHandler extends SimpleChannelInboundHandler<VRequest> {

    private final RequestHandlerFactory requestHandlerFactory;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, VRequest vRequest) {
        log.info("== Received VRequest with correlationId {} and type {} ==", vRequest.correlationId(), vRequest.requestMessageType());
        RequestHandler requestHandler = requestHandlerFactory.getRequestHandler(vRequest);

        ListenableFuture<VResponse> vResponseFuture = requestHandler.handle(vRequest);
        Futures.addCallback(vResponseFuture, new FutureCallback<VResponse>() {
            @Override
            public void onSuccess(@Nonnull VResponse vResponse) {
                ByteBuf responseByteBuf = Unpooled.wrappedBuffer(vResponse.getByteBuffer());
                Response response = new Response(responseByteBuf.readableBytes(), responseByteBuf);

                ChannelFuture channelFuture = ctx.writeAndFlush(response);
                if (vResponse.responseMessageType() == ResponseMessage.ProduceResponse) {
                    //for now, we want to close the channel for produce request when response is written
                    channelFuture.addListener(ChannelFutureListener.CLOSE);
                }
            }

            @Override
            public void onFailure(@Nonnull Throwable throwable) {
                exceptionCaught(ctx, throwable);
            }
        }, MoreExecutors.directExecutor());
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
