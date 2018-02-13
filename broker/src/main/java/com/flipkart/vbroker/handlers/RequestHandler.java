package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.entities.VResponse;

import java.util.concurrent.CompletionStage;

public interface RequestHandler {

    CompletionStage<VResponse> handle(VRequest vRequest);
}
