package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.TopicCreateResponse;
import com.flipkart.vbroker.entities.VResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicCreateResponseHandler implements ResponseHandler {

    @Override
    public void handle(VResponse vResponse) {
        TopicCreateResponse topicCreateResponse = (TopicCreateResponse) vResponse
                .responseMessage(new TopicCreateResponse());
        log.info("Response code for handling topic create is {}", topicCreateResponse.statusCode());

    }
}