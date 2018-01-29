package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.entities.VResponse;

public interface RequestHandler {

    VResponse handle(VRequest vRequest);
}
