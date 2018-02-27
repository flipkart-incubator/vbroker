package com.flipkart.vbroker.services;

import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.entities.TopicCategory;
import com.google.flatbuffers.FlatBufferBuilder;

public class TopicTestUtils {

    /**
     * Creates and returns a new topic entity with dummy data.
     *
     * @return
     */
    public static Topic dummyTopic() {

        FlatBufferBuilder topicBuilder = new FlatBufferBuilder();
        int topicNameOffset = topicBuilder.createString("topic1");
        int topicOffset = Topic.createTopic(topicBuilder, (short) 1, topicNameOffset, true, (short) 1, (short) 3,
            TopicCategory.TOPIC);
        topicBuilder.finish(topicOffset);
        return Topic.getRootAsTopic(topicBuilder.dataBuffer());
    }
}
