package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.entities.VResponse;

import java.util.concurrent.CompletionStage;

/**
 * Created by kaushal.hooda on 27/02/18.
 */
public class GetSubscriptionsRequestHandler implements RequestHandler {
    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        return null;
    }
}
