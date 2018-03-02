package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.wrappers.Topic;

public interface Partitioner {

    TopicPartition partition(Topic topic, ProducerRecord record);
}
