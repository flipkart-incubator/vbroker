// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: entities.proto

package com.flipkart.vbroker.proto;

public interface TopicMetadataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto.TopicMetadata)
    com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.proto.ProtoTopic topic = 1;</code>
     */
    boolean hasTopic();

    /**
     * <code>.proto.ProtoTopic topic = 1;</code>
     */
    com.flipkart.vbroker.proto.ProtoTopic getTopic();

    /**
     * <code>.proto.ProtoTopic topic = 1;</code>
     */
    com.flipkart.vbroker.proto.ProtoTopicOrBuilder getTopicOrBuilder();

    /**
     * <code>repeated .proto.PartitionMetadata partitionMetadatas = 2;</code>
     */
    java.util.List<com.flipkart.vbroker.proto.PartitionMetadata>
    getPartitionMetadatasList();

    /**
     * <code>repeated .proto.PartitionMetadata partitionMetadatas = 2;</code>
     */
    com.flipkart.vbroker.proto.PartitionMetadata getPartitionMetadatas(int index);

    /**
     * <code>repeated .proto.PartitionMetadata partitionMetadatas = 2;</code>
     */
    int getPartitionMetadatasCount();

    /**
     * <code>repeated .proto.PartitionMetadata partitionMetadatas = 2;</code>
     */
    java.util.List<? extends com.flipkart.vbroker.proto.PartitionMetadataOrBuilder>
    getPartitionMetadatasOrBuilderList();

    /**
     * <code>repeated .proto.PartitionMetadata partitionMetadatas = 2;</code>
     */
    com.flipkart.vbroker.proto.PartitionMetadataOrBuilder getPartitionMetadatasOrBuilder(
        int index);

    /**
     * <code>repeated .proto.SubscriptionMetadata subscriptionMetadatas = 3;</code>
     */
    java.util.List<com.flipkart.vbroker.proto.SubscriptionMetadata>
    getSubscriptionMetadatasList();

    /**
     * <code>repeated .proto.SubscriptionMetadata subscriptionMetadatas = 3;</code>
     */
    com.flipkart.vbroker.proto.SubscriptionMetadata getSubscriptionMetadatas(int index);

    /**
     * <code>repeated .proto.SubscriptionMetadata subscriptionMetadatas = 3;</code>
     */
    int getSubscriptionMetadatasCount();

    /**
     * <code>repeated .proto.SubscriptionMetadata subscriptionMetadatas = 3;</code>
     */
    java.util.List<? extends com.flipkart.vbroker.proto.SubscriptionMetadataOrBuilder>
    getSubscriptionMetadatasOrBuilderList();

    /**
     * <code>repeated .proto.SubscriptionMetadata subscriptionMetadatas = 3;</code>
     */
    com.flipkart.vbroker.proto.SubscriptionMetadataOrBuilder getSubscriptionMetadatasOrBuilder(
        int index);
}
