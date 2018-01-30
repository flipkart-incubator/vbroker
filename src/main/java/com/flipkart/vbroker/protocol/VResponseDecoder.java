package com.flipkart.vbroker.protocol;

import com.flipkart.vbroker.entities.VResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class VResponseDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        log.debug("Decoding VResponse bytebuf");
        int responseLength = in.readInt();
        ByteBuf byteBuf = in.readBytes(responseLength);
        VResponse response = VResponse.getRootAsVResponse(byteBuf.nioBuffer());
        out.add(response);
    }
}
