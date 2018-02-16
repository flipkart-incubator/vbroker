package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.entities.Message;

public class RedisMessageObject extends RedisObject {
    private Message message;

    public RedisMessageObject(Message message) {
        super(ObjectType.MESSAGE);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
