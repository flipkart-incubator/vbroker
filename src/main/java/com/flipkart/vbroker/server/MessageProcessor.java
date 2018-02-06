package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.MessageWithGroup;

public interface MessageProcessor {

    void process(MessageWithGroup messageWithGroup) throws Exception;
}
