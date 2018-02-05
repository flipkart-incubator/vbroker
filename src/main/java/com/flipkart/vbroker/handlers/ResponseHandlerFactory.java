package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.ResponseMessage;
import com.flipkart.vbroker.entities.VResponse;
import com.flipkart.vbroker.exceptions.VBrokerException;
import io.netty.bootstrap.Bootstrap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ResponseHandlerFactory {

    private final Bootstrap clientBootstrap;

    public ResponseHandler getResponseHandler(VResponse msg) {
        ResponseHandler responseHandler;

        switch (msg.responseMessageType()) {
            case ResponseMessage.ProduceResponse:
                responseHandler = new ProduceResponseHandler();
                break;
            case ResponseMessage.FetchResponse:
                responseHandler = new FetchResponseHandler(clientBootstrap);
                break;
            case ResponseMessage.TopicCreateResponse:
                responseHandler = new TopicCreateResponseHandler();
                break;
            case ResponseMessage.SubscriptionCreateResponse:
            	responseHandler = new SubscriptionCreateResponseHandler();
            	break;
            default:
                throw new VBrokerException("Unsupported ResponseMessageType: " + msg.responseMessageType());
        }

        return responseHandler;
    }
}
