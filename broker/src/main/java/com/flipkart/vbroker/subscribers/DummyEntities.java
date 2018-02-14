package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.entities.*;
import com.google.flatbuffers.FlatBufferBuilder;

public class DummyEntities {

    public static Topic topic1;
    public static Subscription subscription1;

    static {
        FlatBufferBuilder topicBuilder = new FlatBufferBuilder();
        int topicOffset = Topic.createTopic(topicBuilder,
            (short) 101,
            topicBuilder.createString("topic_1"),
            true,
            (short) 1,
            (short) 1,
            TopicType.MAIN,
            TopicCategory.QUEUE);
        topicBuilder.finish(topicOffset);
        topic1 = Topic.getRootAsTopic(topicBuilder.dataBuffer());

        FlatBufferBuilder subBuilder = new FlatBufferBuilder();

        int codeRangeOffset = CodeRange.createCodeRange(subBuilder, (short) 200, (short) 299);
        int codeRangesVectorOffset = CallbackConfig.createCodeRangesVector(subBuilder, new int[]{codeRangeOffset});
        int callbackConfigOffset = CallbackConfig.createCallbackConfig(subBuilder, codeRangesVectorOffset);

        int subscriptionOffset = Subscription.createSubscription(subBuilder,
            (short) 1001,
            topic1.topicId(),
            subBuilder.createString("subscription_1"),
            true,
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
        subBuilder.finish(subscriptionOffset);
        subscription1 = Subscription.getRootAsSubscription(subBuilder.dataBuffer());
    }
}
