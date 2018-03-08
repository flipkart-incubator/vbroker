package com.flipkart.vbroker.protocol;

import com.flipkart.vbroker.flatbuf.VRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class VRequestDecoder extends ReplayingDecoder<Void> {

    public VRequestDecoder() {
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        log.debug("Decoding VRequest bytebuf");

        int requestLength = in.readInt();
        ByteBuf byteBuf = in.readBytes(requestLength);
        VRequest request = VRequest.getRootAsVRequest(byteBuf.nioBuffer());
        out.add(request);
    }
}
