package com.flipkart.vbroker.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class VRequestDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        log.info("Decoding VRequest bytebuf");

        VRequest request = new VRequest();
        request.setVersion(in.readShort());
        log.info("read version");

        request.setApiKey(in.readShort());
        log.info("read apiKey");

        int requestLength = in.readInt();
        log.info("read reqLength as {}", requestLength);

        request.setRequestLength(requestLength);
        request.setRequestPayload(in.readBytes(requestLength));
        log.info("read reqPayload");

        // 1 => ProduceRequest
        if (request.getApiKey() == 1) {
            log.info("Decoded request is a ProduceRequest");
        }

        log.info("Received VRequest: {}", request);
        out.add(request);
    }
}
