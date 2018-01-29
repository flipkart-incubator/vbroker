package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.VResponse;

public interface ResponseHandler {

    void handle(VResponse vResponse);
}
