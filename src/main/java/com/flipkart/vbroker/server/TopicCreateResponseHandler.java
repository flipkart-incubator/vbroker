package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.TopicCreateResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class TopicCreateResponseHandler implements ResponseHandler {

    private final TopicCreateResponse topicCreateResponse;

    @Override
    public void handle() {
        log.info("Response code for handling topic create is {}", topicCreateResponse.statusCode());
    }
}
