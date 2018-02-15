package com.flipkart.vbroker.data.redis;

public class RedisObject {
    private ObjectType objectType;

    public RedisObject(ObjectType objectType) {
        this.objectType = objectType;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    enum ObjectType {
        MESSAGE, STRING;
    }
}
