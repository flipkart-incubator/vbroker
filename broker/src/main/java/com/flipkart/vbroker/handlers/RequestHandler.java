package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;

import java.util.concurrent.CompletionStage;

public interface RequestHandler {

    CompletionStage<VResponse> handle(VRequest vRequest);
}
