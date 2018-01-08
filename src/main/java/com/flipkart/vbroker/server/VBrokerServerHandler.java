package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.protocol.Response;
import com.google.common.base.Charsets;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class VBrokerServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("== ChannelRead0 ==");

        FlatBufferBuilder builder = new FlatBufferBuilder();
        int produceResponse = ProduceResponse.createProduceResponse(
                builder,
                (short) 101,
                (short) 0,
                (short) 200);
        int vResponse = VResponse.createVResponse(builder,
                1001,
                ResponseMessage.ProduceResponse,
                produceResponse
        );
        builder.finish(vResponse);
        ByteBuffer responseByteBuffer = builder.dataBuffer();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(responseByteBuffer);

        Response response = new Response(byteBuf.readableBytes(), byteBuf);

        if (msg instanceof VRequest) {
            VRequest request = (VRequest) msg;
            log.info("== Received VRequest {} with type {} ==", request, request.requestMessageType());

            switch (request.requestMessageType()) {
                case RequestMessage.ProduceRequest:
                    log.info("Request is of type ProduceRequest");
                    ProduceRequest produceRequest = (ProduceRequest) request.requestMessage(new ProduceRequest());
                    assert produceRequest != null;
                    log.info("Getting messageSet for topic {} and partition {}", produceRequest.topicId(), produceRequest.partitionId());
                    MessageSet messageSet = produceRequest.messageSet();
                    for (int i = 0; i < messageSet.messagesLength(); i++) {
                        Message message = messageSet.messages(i);
                        ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
                        log.info("Decoded msg with msgId: {} and payload: {}", message.messageId(),
                                Charsets.UTF_8.decode(byteBuffer).toString());
                    }
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                    break;
            }
        } else if (msg instanceof Short) {
            log.info("Msg is an instance of short");
            Short aShort = (short) 1;
            ctx.write(aShort).addListener(ChannelFutureListener.CLOSE);
        } else {
            throw new VBrokerException("Unknown msg type - expected VRequest/Short");
        }
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
