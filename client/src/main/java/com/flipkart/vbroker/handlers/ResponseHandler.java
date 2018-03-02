package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.VResponse;

public interface ResponseHandler {

    void handle(VResponse vResponse);
}
