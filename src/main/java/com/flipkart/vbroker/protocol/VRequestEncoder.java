package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class VRequestEncoder extends MessageToByteEncoder<VRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, VRequest msg, ByteBuf out) throws Exception {
        out.writeShort(msg.getVersion());
        out.writeShort(msg.getApiKey());
        out.writeInt(msg.getRequestLength());
        out.writeBytes(msg.getRequestPayload());
    }
}
