package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.Topic;

public class DefaultPartitioner implements Partitioner {

    @Override
    public TopicPartition partition(Message message, Topic topic) {
        return new TopicPartition((short) 0, topic.id(), topic.grouped());
    }
}
