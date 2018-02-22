package com.flipkart.vbroker.server;

import com.flipkart.vbroker.subscribers.IterableMessage;

public interface MessageProcessor {

    void process(IterableMessage messageWithGroup) throws Exception;
}
