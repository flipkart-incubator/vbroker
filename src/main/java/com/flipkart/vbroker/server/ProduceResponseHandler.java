package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.ProduceResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ProduceResponseHandler implements ResponseHandler {

    private final ProduceResponse produceResponse;

    @Override
    public void handle() {
        log.info("Handling fetchResponse for topic {} and partition {}", produceResponse.topicId(), produceResponse.partitionId());
        log.info("Response code for handling produceRequest is {}", produceResponse.statusCode());
    }
}
