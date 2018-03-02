// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.flatbuf;

public final class StatusCode {
  private StatusCode() { }
  public static final int None = 0;
  public static final int ProduceSuccess_NoError = 101;
  public static final int ProduceFailed_NoLeader = 151;
  public static final int ProduceFailed_NotEnoughReplicas = 152;
  public static final int Success = 200;
  public static final int ConsumeSuccess_NoError = 201;
  public static final int ConsumeError_DestinationDown = 251;
  public static final int TopicCreateFailed_AlreadyExists = 351;
  public static final int TopicCreateFailed_Validation = 352;
  public static final int SubscriptionCreateFailed_NoTopic = 451;
  public static final int SubscriptionCreateFailed_AlreadyExists = 452;
  public static final int SubscriptionCreateFailed_Validation = 453;
  public static final int QueueCreateFailed_AlreadyExists = 461;
  public static final int QueueCreateFailed_Validation = 462;
  public static final int GetLagFailed = 470;
  public static final int GetLagFailed_NoTopic = 471;
  public static final int GetLagFailed_NoSubscription = 472;
  public static final int GetLagFailed_NoPartition = 473;
  public static final int GetLagFailed_Validation = 474;
  public static final int Invalid_Partitions = 501;
  public static final int Invalid_ReplicationFactor = 502;
  public static final int Leader_Not_Available = 503;
  public static final int Controller_Not_Available = 504;
  public static final int Not_Leader_For_Partition = 505;
  public static final int Entity_Not_Exists = 506;
}

