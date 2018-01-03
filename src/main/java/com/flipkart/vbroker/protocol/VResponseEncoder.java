package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class VResponseEncoder extends MessageToByteEncoder<VResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, VResponse msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getStatus());
    }
}
