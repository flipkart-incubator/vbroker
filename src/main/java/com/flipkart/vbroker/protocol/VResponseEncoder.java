package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VResponseEncoder extends MessageToByteEncoder<Response> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Response msg, ByteBuf out) {
        log.info("Encoding VResponse as bytes");
        out.writeInt(msg.getResponseLength());
        out.writeBytes(msg.getVResponse());
    }
}
