package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.SubscriptionCreateResponse;
import com.flipkart.vbroker.entities.VResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscriptionCreateResponseHandler implements ResponseHandler {

    @Override
    public void handle(VResponse vResponse) {
        SubscriptionCreateResponse subscriptionCreateResponse = (SubscriptionCreateResponse) vResponse
                .responseMessage(new SubscriptionCreateResponse());
        log.info("Response code for handling subscription create is {}", subscriptionCreateResponse.statusCode());

    }

}
