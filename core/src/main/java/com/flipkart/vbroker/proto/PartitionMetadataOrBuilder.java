// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: entities.proto

package com.flipkart.vbroker.proto;

public interface PartitionMetadataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto.PartitionMetadata)
    com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int32 id = 1;</code>
     */
    int getId();

    /**
     * <code>int32 topicId = 2;</code>
     */
    int getTopicId();

    /**
     * <code>int32 leaderId = 3;</code>
     */
    int getLeaderId();

    /**
     * <code>repeated int32 replicas = 4;</code>
     */
    java.util.List<java.lang.Integer> getReplicasList();

    /**
     * <code>repeated int32 replicas = 4;</code>
     */
    int getReplicasCount();

    /**
     * <code>repeated int32 replicas = 4;</code>
     */
    int getReplicas(int index);
}
