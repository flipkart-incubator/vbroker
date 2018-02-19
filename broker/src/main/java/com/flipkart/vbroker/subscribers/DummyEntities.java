package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.entities.*;
import com.google.flatbuffers.FlatBufferBuilder;

public class DummyEntities {

    public static Topic groupedTopic;
    public static Subscription groupedSubscription;

    public static Topic unGroupedTopic;
    public static Subscription unGroupedSubscription;

    static {
        FlatBufferBuilder topicBuilder = new FlatBufferBuilder();
        int topicOffset = Topic.createTopic(topicBuilder,
            (short) 101,
            topicBuilder.createString("grouped_topic_1"),
            true,
            (short) 1,
            (short) 1,
            TopicType.MAIN,
            TopicCategory.QUEUE
        );
        topicBuilder.finish(topicOffset);
        groupedTopic = Topic.getRootAsTopic(topicBuilder.dataBuffer());

        topicBuilder = new FlatBufferBuilder();
        topicOffset = Topic.createTopic(topicBuilder,
            (short) 151,
            topicBuilder.createString("un_grouped_topic_1"),
            false,
            (short) 1,
            (short) 1,
            TopicType.MAIN,
            TopicCategory.QUEUE
        );
        topicBuilder.finish(topicOffset);
        unGroupedTopic = Topic.getRootAsTopic(topicBuilder.dataBuffer());

        FlatBufferBuilder subBuilder = new FlatBufferBuilder();
        int subscriptionOffset = getSubscriptionOffset(subBuilder, (short) 201, groupedTopic);
        subBuilder.finish(subscriptionOffset);
        groupedSubscription = Subscription.getRootAsSubscription(subBuilder.dataBuffer());

        subBuilder = new FlatBufferBuilder();
        subscriptionOffset = getSubscriptionOffset(subBuilder, (short) 251, unGroupedTopic);
        subBuilder.finish(subscriptionOffset);
        unGroupedSubscription = Subscription.getRootAsSubscription(subBuilder.dataBuffer());
    }

    private static int getSubscriptionOffset(FlatBufferBuilder subBuilder,
                                             short subscriptionId,
                                             Topic topic) {
        int codeRangeOffset = CodeRange.createCodeRange(subBuilder, (short) 200, (short) 299);
        int codeRangesVectorOffset = CallbackConfig.createCodeRangesVector(subBuilder, new int[]{codeRangeOffset});
        int callbackConfigOffset = CallbackConfig.createCallbackConfig(subBuilder, codeRangesVectorOffset);
        return Subscription.createSubscription(subBuilder,
            subscriptionId,
            groupedTopic.topicId(),
            subBuilder.createString("subscription_" + subscriptionId),
            topic.grouped(),
            (short) 1,
            (short) 60000,
            SubscriptionType.DYNAMIC,
            SubscriptionMechanism.PUSH,
            subBuilder.createString("http://localhost:13000/messages"),
            subBuilder.createString("POST"),
            true,
            subBuilder.createString("NOR"),
            subBuilder.createString(""),
            callbackConfigOffset
        );
    }
}
