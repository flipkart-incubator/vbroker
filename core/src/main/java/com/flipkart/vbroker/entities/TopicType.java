// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

public final class TopicType {
    public static final byte MAIN = 0;
    public static final byte SIDELINE = 1;
    public static final byte RETRY = 2;
    public static final String[] names = {"MAIN", "SIDELINE", "RETRY",};

    private TopicType() {
    }

    public static String name(int e) {
        return names[e];
    }
}

