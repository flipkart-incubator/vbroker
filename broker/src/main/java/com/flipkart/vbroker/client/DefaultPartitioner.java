package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.utils.TopicUtils;

public class DefaultPartitioner implements Partitioner {

    @Override
    public TopicPartition partition(Topic topic, ProducerRecord record) {
        //fix this - by default returns first partition for now
        return TopicUtils.getTopicPartition(topic, (short) 0);
    }
}
