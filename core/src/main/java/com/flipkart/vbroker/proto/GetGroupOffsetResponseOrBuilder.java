// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: responses.proto

package com.flipkart.vbroker.proto;

public interface GetGroupOffsetResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto.GetGroupOffsetResponse)
    com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string groupId = 1;</code>
     */
    java.lang.String getGroupId();

    /**
     * <code>string groupId = 1;</code>
     */
    com.google.protobuf.ByteString
    getGroupIdBytes();

    /**
     * <code>int32 offset = 2;</code>
     */
    int getOffset();

    /**
     * <code>.proto.VStatus status = 3;</code>
     */
    boolean hasStatus();

    /**
     * <code>.proto.VStatus status = 3;</code>
     */
    com.flipkart.vbroker.proto.VStatus getStatus();

    /**
     * <code>.proto.VStatus status = 3;</code>
     */
    com.flipkart.vbroker.proto.VStatusOrBuilder getStatusOrBuilder();
}
