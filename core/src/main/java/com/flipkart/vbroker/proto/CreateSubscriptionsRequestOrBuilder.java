// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: requests.proto

package com.flipkart.vbroker.proto;

public interface CreateSubscriptionsRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto.CreateSubscriptionsRequest)
    com.google.protobuf.MessageOrBuilder {

    /**
     * <code>repeated .proto.ProtoSubscription subscriptions = 1;</code>
     */
    java.util.List<com.flipkart.vbroker.proto.ProtoSubscription>
    getSubscriptionsList();

    /**
     * <code>repeated .proto.ProtoSubscription subscriptions = 1;</code>
     */
    com.flipkart.vbroker.proto.ProtoSubscription getSubscriptions(int index);

    /**
     * <code>repeated .proto.ProtoSubscription subscriptions = 1;</code>
     */
    int getSubscriptionsCount();

    /**
     * <code>repeated .proto.ProtoSubscription subscriptions = 1;</code>
     */
    java.util.List<? extends com.flipkart.vbroker.proto.ProtoSubscriptionOrBuilder>
    getSubscriptionsOrBuilderList();

    /**
     * <code>repeated .proto.ProtoSubscription subscriptions = 1;</code>
     */
    com.flipkart.vbroker.proto.ProtoSubscriptionOrBuilder getSubscriptionsOrBuilder(
        int index);
}
