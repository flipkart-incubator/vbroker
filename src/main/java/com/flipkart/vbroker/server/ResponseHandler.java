package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.VResponse;

public interface ResponseHandler {

    void handle(VResponse vResponse);
}
