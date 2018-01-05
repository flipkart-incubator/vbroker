package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.protocol.VRequest;
import com.flipkart.vbroker.protocol.VResponse;
import com.flipkart.vbroker.protocol.apis.ProduceRequest;
import com.google.common.base.Charsets;
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

        VResponse response = new VResponse();
        int correlationId = 1001;
        response.setCorrelationId(correlationId);
        String respMsg = String.format("VResponse for msg with correlationId: %d", correlationId);
        response.setResponseLength(respMsg.length());
        response.setResponsePayload(Unpooled.wrappedBuffer(respMsg.getBytes()));

        if (msg instanceof ProduceRequest) {
            log.info("== Received ProduceRequest ==");
            ProduceRequest request = (ProduceRequest) msg;
            Message message = request.getMessage();
            ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
            log.info("Decoded msg with msgId: {} and payload: {};", message.messageId(),
                    Charsets.UTF_8.decode(byteBuffer).toString());
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else if (msg instanceof VRequest) {
            log.info("== Received VRequest ==");
            VRequest request = (VRequest) msg;

            Message decodedMsg = Message.getRootAsMessage(request.getRequestPayload().nioBuffer());
            ByteBuffer payloadByteBuf = decodedMsg.bodyPayloadAsByteBuffer();
            int payloadLength = decodedMsg.bodyPayloadLength();
            byte[] bytes = new byte[1024];
            payloadByteBuf.asReadOnlyBuffer().get(bytes, 0, payloadLength);
            log.info("Decoded msg with msgId: {} and payload: {};", decodedMsg.messageId(), new String(bytes));
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else if (msg instanceof Short) {
            log.info("Msg is an instance of short");
            Short aShort = (short) 1;
            ctx.write(aShort).addListener(ChannelFutureListener.CLOSE);
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
