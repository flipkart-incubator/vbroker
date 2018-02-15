package com.flipkart.vbroker.data.redis;

public class RedisStringObject extends RedisObject{
    private String stringData;

    public RedisStringObject(String stringData) {
        super(ObjectType.STRING);
        this.stringData = stringData;
    }

    public String getStringData() {
        return stringData;
    }
}
