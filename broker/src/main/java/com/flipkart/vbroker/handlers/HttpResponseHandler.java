package com.flipkart.vbroker.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse httpResponse) {
        ByteBuf content = httpResponse.content();
        log.info("== Got HttpResponse with status {} and content {} ==", httpResponse.status(), content);

        int statusCode = httpResponse.status().code();
        if (statusCode >= 200 && statusCode < 300) {
            log.info("Success in making httpRequest. Message processing now complete");
        } else if (statusCode >= 400 && statusCode < 500) {
            log.info("Response is 4xx. Sidelining the message");
        } else if (statusCode >= 500 && statusCode < 600) {
            log.info("Response is 5xx. Retrying the message");
        }
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
