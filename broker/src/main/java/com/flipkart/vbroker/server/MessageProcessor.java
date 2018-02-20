package com.flipkart.vbroker.server;

import com.flipkart.vbroker.subscribers.MessageWithMetadata;

public interface MessageProcessor {

    void process(MessageWithMetadata messageWithGroup) throws Exception;
}
