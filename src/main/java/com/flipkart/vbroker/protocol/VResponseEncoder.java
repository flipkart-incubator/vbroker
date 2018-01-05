package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class VResponseEncoder extends MessageToByteEncoder<VResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, VResponse msg, ByteBuf out) throws Exception {
        int correlationId = 1001;
        String sampleResponse = String.format("This is the response payload for request with correlationId: %d", correlationId);
        byte[] bytes = sampleResponse.getBytes();

        out.writeInt(correlationId);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
