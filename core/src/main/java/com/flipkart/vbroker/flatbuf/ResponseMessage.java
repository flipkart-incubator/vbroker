// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.flatbuf;

public final class ResponseMessage {
    public static final byte NONE = 0;
    public static final byte ProduceResponse = 1;
    public static final byte FetchResponse = 2;
    public static final byte ControlResponse = 3;
    public static final String[] names = {"NONE", "ProduceResponse", "FetchResponse", "ControlResponse",};

    private ResponseMessage() {
    }

    public static String name(int e) {
        return names[e];
    }
}
