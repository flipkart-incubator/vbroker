package com.flipkart.vbroker.utils;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.Subscription;
import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;

public class DummyEntities {

    public static Topic topic1;
    public static Subscription subscription1;

    static {
        topic1 = new Topic((short) 101);
        TopicPartition topicPartition1 = new TopicPartition((short) 0);
        topic1.addPartition(topicPartition1);
        subscription1 = new Subscription((short) 1001, topic1, true);
        PartSubscription partSubscription1 = new PartSubscription((short) 0, topicPartition1);
        subscription1.addPartSubscription(partSubscription1);
    }
}
