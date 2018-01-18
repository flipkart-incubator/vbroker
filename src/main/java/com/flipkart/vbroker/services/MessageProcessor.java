package com.flipkart.vbroker.services;

import com.flipkart.vbroker.entities.Message;

public interface MessageProcessor {

    public void process(Message message);
}
