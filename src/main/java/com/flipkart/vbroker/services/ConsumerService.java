package com.flipkart.vbroker.services;

import com.flipkart.vbroker.entities.Message;

public interface ConsumerService {

    public Message consumeMessage();
}
