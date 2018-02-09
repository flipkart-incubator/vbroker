// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

public final class StatusCode {
    public static final short None = 0;
    public static final short ProduceSuccess_NoError = 101;
    public static final short ProduceFailed_NoLeader = 151;
    public static final short ProduceFailed_NotEnoughReplicas = 152;
    public static final short ConsumeSuccess_NoError = 201;
    public static final short ConsumeError_DestinationDown = 251;
    public static final short Success = 301;
    public static final short TopicCreateFailed_AlreadyExists = 351;
    public static final short TopicCreateFailed_Validation = 352;
    public static final short SubscriptionCreateFailed_NoTopic = 451;
    public static final short SubscriptionCreateFailed_AlreadyExists = 452;
    public static final short SubscriptionCreateFailed_Validation = 453;

    private StatusCode() {
    }
}

