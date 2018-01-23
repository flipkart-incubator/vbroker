package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.FetchResponse;
import com.flipkart.vbroker.entities.ProduceResponse;
import com.flipkart.vbroker.entities.ResponseMessage;
import com.flipkart.vbroker.entities.VResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ResponseHandlerFactory {

    private final Bootstrap clientBootstrap;

    public ResponseHandler getResponseHandler(VResponse msg, ChannelHandlerContext ctx) {
        ResponseHandler responseHandler = null;

        switch (msg.responseMessageType()) {
            case ResponseMessage.ProduceResponse:
                ProduceResponse produceResponse = (ProduceResponse) msg.responseMessage(new ProduceResponse());
                assert produceResponse != null;
                //log.info("Received ProduceResponse with statusCode {}", produceResponse.statusCode());
                responseHandler = new ProduceResponseHandler(produceResponse);
                break;
            case ResponseMessage.FetchResponse:
                FetchResponse fetchResponse = (FetchResponse) msg.responseMessage(new FetchResponse());
                assert fetchResponse != null;
                log.info("Received FetchResponse with statusCode {}", fetchResponse.statusCode());
                responseHandler = new FetchResponseHandler(clientBootstrap, fetchResponse);
                //ctx.close().addListener(ChannelFutureListener.CLOSE);
        }

        return responseHandler;
    }
}
