package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class VResponseDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        VResponse response = new VResponse();
        response.setCorrelationId(in.readInt());
        response.setResponseLength(in.readInt());
        //TODO: check if the buf.readBytes() below creates a new bytebuf and change it to not create one
        response.setResponsePayload(in.readBytes(response.getResponseLength()));
        out.add(response);
    }
}
