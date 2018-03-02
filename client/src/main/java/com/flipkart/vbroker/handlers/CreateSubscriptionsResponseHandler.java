package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.CreateSubscriptionsResponse;
import com.flipkart.vbroker.utils.FlatbufUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateSubscriptionsResponseHandler implements ResponseHandler {

    @Override
    public void handle(VResponse vResponse) {
        CreateSubscriptionsResponse createSubscriptionsResponse = FlatbufUtils.getProtoResponse(vResponse).getCreateSubscriptionsResponse();
        log.info("Response code for handling subscriptions create is {}", createSubscriptionsResponse.toString());
    }
}
