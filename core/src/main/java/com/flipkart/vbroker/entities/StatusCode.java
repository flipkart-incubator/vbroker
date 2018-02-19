// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

public final class StatusCode {
  private StatusCode() { }
  public static final short None = 0;
  public static final short ProduceSuccess_NoError = 101;
  public static final short ProduceFailed_NoLeader = 151;
  public static final short ProduceFailed_NotEnoughReplicas = 152;
  public static final short Success = 200;
  public static final short ConsumeSuccess_NoError = 201;
  public static final short ConsumeError_DestinationDown = 251;
  public static final short TopicCreateFailed_AlreadyExists = 351;
  public static final short TopicCreateFailed_Validation = 352;
  public static final short SubscriptionCreateFailed_NoTopic = 451;
  public static final short SubscriptionCreateFailed_AlreadyExists = 452;
  public static final short SubscriptionCreateFailed_Validation = 453;
  public static final short QueueCreateFailed_AlreadyExists = 461;
  public static final short QueueCreateFailed_Validation = 462;
  public static final short GetLagFailed_NoTopic = 471;
  public static final short GetLagFailed_NoSubscription = 472;
  public static final short GetLagFailed_NoPartition = 473;
  public static final short GetLagFailed_Validation = 474;
  public static final short Invalid_Partitions = 501;
  public static final short Invalid_ReplicationFactor = 502;
  public static final short Leader_Not_Available = 503;
  public static final short Controller_Not_Available = 504;
  public static final short Not_Leader_For_Partition = 505;
  public static final short Entity_Not_Exists = 506;
}

