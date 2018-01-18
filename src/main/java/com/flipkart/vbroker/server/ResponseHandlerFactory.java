package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.FetchResponse;
import com.flipkart.vbroker.entities.ProduceResponse;
import com.flipkart.vbroker.entities.ResponseMessage;
import com.flipkart.vbroker.entities.VResponse;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseHandlerFactory {

    public ResponseHandler getResponseHandler(VResponse msg, ChannelHandlerContext ctx) {
        ResponseHandler responseHandler = null;

        switch (msg.responseMessageType()) {
            case ResponseMessage.ProduceResponse:
                ProduceResponse produceResponse = (ProduceResponse) msg.responseMessage(new ProduceResponse());
                assert produceResponse != null;
                short statusCode = produceResponse.statusCode();
                log.info("Received ProduceResponse with statusCode {}", statusCode);
                break;
            case ResponseMessage.FetchResponse:
                FetchResponse fetchResponse = (FetchResponse) msg.responseMessage(new FetchResponse());
                assert fetchResponse != null;
                log.info("Received FetchResponse with statusCode {}", fetchResponse.statusCode());
                responseHandler = new FetchResponseHandler(fetchResponse);
        }

        return responseHandler;
    }
}
