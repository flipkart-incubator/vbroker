package com.flipkart.vbroker.protocol;

import com.flipkart.vbroker.protocol.apis.ProduceRequest;
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

        //VRequest.ApiKey

        request.setApiKey(VRequest.ApiKey.getKey(in.readShort()));
        log.info("read apiKey");

        request.setCorrelationId(in.readInt());
        log.info("read correlationId");

        int requestLength = in.readInt();
        log.info("read reqLength as {}", requestLength);

        request.setRequestLength(requestLength);
        request.setRequestPayload(in.readBytes(requestLength));
        log.info("read reqPayload");

        // 1 => ProduceRequest
        if (request instanceof ProduceRequest ||
                VRequest.ApiKey.PRODUCE_REQUEST.equals(request.getApiKey())) {
            log.info("Decoded request is a ProduceRequest");
        }

        log.info("Received VRequest: {}", request);
        out.add(request);
    }
}
