// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

public final class RequestMessage {
    private RequestMessage() {
    }

    public static final byte NONE = 0;
    public static final byte ProduceRequest = 1;
    public static final byte FetchRequest = 2;
    public static final byte TopicCreateRequest = 3;

    public static final String[] names = {"NONE", "ProduceRequest", "FetchRequest", "TopicCreateRequest",};

    public static String name(int e) {
        return names[e];
    }
}

