// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: responses.proto

package com.flipkart.vbroker.proto;

public interface CreateQueueResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto.CreateQueueResponse)
    com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     * is the same as name of the queue's only topic.
     * </pre>
     * <p>
     * <code>string queueName = 1;</code>
     */
    java.lang.String getQueueName();

    /**
     * <pre>
     * is the same as name of the queue's only topic.
     * </pre>
     * <p>
     * <code>string queueName = 1;</code>
     */
    com.google.protobuf.ByteString
    getQueueNameBytes();

    /**
     * <code>.proto.VStatus status = 2;</code>
     */
    boolean hasStatus();

    /**
     * <code>.proto.VStatus status = 2;</code>
     */
    com.flipkart.vbroker.proto.VStatus getStatus();

    /**
     * <code>.proto.VStatus status = 2;</code>
     */
    com.flipkart.vbroker.proto.VStatusOrBuilder getStatusOrBuilder();
}