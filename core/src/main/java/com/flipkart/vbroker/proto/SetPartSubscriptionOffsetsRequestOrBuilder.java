// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: requests.proto

package com.flipkart.vbroker.proto;

public interface SetPartSubscriptionOffsetsRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto.SetPartSubscriptionOffsetsRequest)
    com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int32 partitionId = 1;</code>
     */
    int getPartitionId();

    /**
     * <code>repeated .proto.GroupOffset groupOffsets = 2;</code>
     */
    java.util.List<com.flipkart.vbroker.proto.GroupOffset>
    getGroupOffsetsList();

    /**
     * <code>repeated .proto.GroupOffset groupOffsets = 2;</code>
     */
    com.flipkart.vbroker.proto.GroupOffset getGroupOffsets(int index);

    /**
     * <code>repeated .proto.GroupOffset groupOffsets = 2;</code>
     */
    int getGroupOffsetsCount();

    /**
     * <code>repeated .proto.GroupOffset groupOffsets = 2;</code>
     */
    java.util.List<? extends com.flipkart.vbroker.proto.GroupOffsetOrBuilder>
    getGroupOffsetsOrBuilderList();

    /**
     * <code>repeated .proto.GroupOffset groupOffsets = 2;</code>
     */
    com.flipkart.vbroker.proto.GroupOffsetOrBuilder getGroupOffsetsOrBuilder(
        int index);
}
