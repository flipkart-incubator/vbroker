package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.ProduceResponse;
import com.flipkart.vbroker.entities.ResponseMessage;
import com.flipkart.vbroker.entities.VResponse;
import com.flipkart.vbroker.protocol.Response;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class VBrokerServerResponseHandler extends SimpleChannelInboundHandler<HttpResponse> {
    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpResponse msg) throws Exception {
        log.info("== Got HttpResponse with status {} ==", msg.status());

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

        Response response = new Response(byteBuf.readableBytes(), byteBuf);
        ctx.write(response);//.addListener(ChannelFutureListener.CLOSE);
    }
}