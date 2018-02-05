package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.Message;

public interface MessageProcessor {

    void process(Message message);
}
