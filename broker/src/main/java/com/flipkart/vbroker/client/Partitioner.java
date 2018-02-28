package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;

public interface Partitioner {

    TopicPartition partition(ProducerRecord record);
}
