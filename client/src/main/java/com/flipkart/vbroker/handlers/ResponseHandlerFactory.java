package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.ResponseMessage;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.utils.FlatbufUtils;
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
            case ResponseMessage.ControlResponse:
                responseHandler = getProtoResponseHandler(FlatbufUtils.getProtoResponse(msg));
                break;
            default:
                throw new VBrokerException("Unsupported ResponseMessageType: " + msg.responseMessageType());
        }

        return responseHandler;
    }

    private ResponseHandler getProtoResponseHandler(ProtoResponse response) {
        ResponseHandler responseHandler;
        switch (response.getProtoResponseCase()) {
            case CREATESUBSCRIPTIONSRESPONSE:
                responseHandler = new CreateSubscriptionsResponseHandler();
                break;
            case CREATETOPICSRESPONSE:
                responseHandler = new CreateTopicsResponseHandler();
                break;
            case GETSUBSCRIPTIONSRESPONSE:
                responseHandler = new GetSubscriptionLagsResponseHandler();
            default:
                throw new VBrokerException("Unsupported ProtoResponseType: " + response.getProtoResponseCase());
        }
        return responseHandler;
    }
}
