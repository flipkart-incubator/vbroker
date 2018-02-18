package com.flipkart.vbroker.server;

import com.flipkart.vbroker.subscribers.GroupedMessageWithGroup;

public interface MessageProcessor {

    void process(GroupedMessageWithGroup messageWithGroup) throws Exception;
}
