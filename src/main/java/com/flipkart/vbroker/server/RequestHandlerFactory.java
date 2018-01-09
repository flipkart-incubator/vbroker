package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.ProduceRequest;
import com.flipkart.vbroker.entities.RequestMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.exceptions.VBrokerException;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RequestHandlerFactory {

    private final ChannelHandlerContext ctx;

    public RequestHandler getRequestHandler(VRequest request) {
        RequestHandler requestHandler;
        switch (request.requestMessageType()) {
            case RequestMessage.ProduceRequest:
                log.info("Request is of type ProduceRequest");
                ProduceRequest produceRequest = (ProduceRequest) request.requestMessage(new ProduceRequest());
                requestHandler = new ProduceRequestHandler(ctx, produceRequest);
                break;
            default:
                throw new VBrokerException("Unknown RequestMessageType: " + request.requestMessageType());
        }
        return requestHandler;
    }
}
