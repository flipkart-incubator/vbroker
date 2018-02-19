package com.flipkart.vbroker.server;

import com.flipkart.vbroker.subscribers.IMessageWithGroup;

public interface MessageProcessor {

    void process(IMessageWithGroup messageWithGroup) throws Exception;
}
