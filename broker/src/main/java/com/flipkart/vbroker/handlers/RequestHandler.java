package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.entities.VResponse;
import com.google.common.util.concurrent.ListenableFuture;

public interface RequestHandler {

    ListenableFuture<VResponse> handle(VRequest vRequest);
}
