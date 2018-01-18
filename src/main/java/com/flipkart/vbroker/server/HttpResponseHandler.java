package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.ProduceResponse;
import com.flipkart.vbroker.entities.ResponseMessage;
import com.flipkart.vbroker.entities.VResponse;
import com.flipkart.vbroker.protocol.Response;
import com.google.common.base.Charsets;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@AllArgsConstructor
@Slf4j
public class HttpResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

    //private final ChannelHandlerContext gCtx;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;
            log.info("== Got HttpResponse with status {} ==", httpResponse.status());
        }

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            log.info(Charsets.UTF_8.decode(content.content().nioBuffer()).toString());

            if (content instanceof LastHttpContent) {
                log.info("== END OF CONTENT ==");

                short topicId = 101;
                short partitionId = 0;
                int correlationId = 1001;

                FlatBufferBuilder builder = new FlatBufferBuilder();
                int produceResponse = ProduceResponse.createProduceResponse(
                        builder,
                        topicId,
                        partitionId,
                        (short) 200);
                int vResponse = VResponse.createVResponse(
                        builder,
                        correlationId,
                        ResponseMessage.ProduceResponse,
                        produceResponse);
                builder.finish(vResponse);
                ByteBuffer responseByteBuffer = builder.dataBuffer();
                ByteBuf byteBuf = Unpooled.wrappedBuffer(responseByteBuffer);

                log.info("Writing Response to gCtx");
                Response response = new Response(byteBuf.readableBytes(), byteBuf);
                //gCtx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                //log.info("Done writing Response to gCtx");
                ctx.close();
            }
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
