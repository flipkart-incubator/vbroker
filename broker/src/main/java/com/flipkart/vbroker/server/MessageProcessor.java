package com.flipkart.vbroker.server;

import com.flipkart.vbroker.subscribers.MessageWithGroup;

public interface MessageProcessor {

    void process(MessageWithGroup messageWithGroup) throws Exception;
}
