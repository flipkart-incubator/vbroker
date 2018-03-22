package com.flipkart.vbroker.utils;

import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.wrappers.Subscription;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.flatbuffers.FlatBufferBuilder;

public class DummyEntities {

    public static Topic groupedTopic;
    public static Subscription groupedSubscription;

    public static Topic unGroupedTopic;
    public static Subscription unGroupedSubscription;

    static {
        FlatBufferBuilder topicBuilder = FlatbufUtils.newBuilder();
        groupedTopic = new Topic(ProtoTopic.newBuilder()
            .setId(101)
            .setName("grouped_topic_1")
            .setGrouped(true)
            .setPartitions(1)
            .setReplicationFactor(1)
            .setTopicCategory(TopicCategory.QUEUE)
            .build());

        unGroupedTopic = new Topic(ProtoTopic.newBuilder()
            .setId(151)
            .setName("un_grouped_topic_1")
            .setGrouped(false)
            .setPartitions(1)
            .setReplicationFactor(1)
            .setTopicCategory(TopicCategory.QUEUE)
            .build());

        groupedSubscription = getSubscription(201, groupedTopic);
        unGroupedSubscription = getSubscription(251, unGroupedTopic);
    }

    private static Subscription getSubscription(int subscriptionId, Topic topic) {
        CodeRange codeRange = CodeRange.newBuilder().setFrom(200).setTo(299).build();
        CallbackConfig callbackConfig = CallbackConfig.newBuilder().addCodeRanges(codeRange).build();

        return new Subscription(ProtoSubscription.newBuilder()
            .setId(subscriptionId)
            .setTopicId(topic.id())
            .setName("subscription_" + subscriptionId)
            .setGrouped(topic.grouped())
            .setSubscriptionType(SubscriptionType.DYNAMIC)
            .setSubscriptionMechanism(SubscriptionMechanism.PUSH)
            .setCallbackConfig(callbackConfig)
            .setHttpUri("http://localhost:13000/messages")
            .setHttpMethod(HttpMethod.POST)
            .setFilterOperator(FilterOperator.NOR)
            .setParallelism(1)
            .setRequestTimeout(60000)
            .setElastic(false).build()
        );
    }
}
