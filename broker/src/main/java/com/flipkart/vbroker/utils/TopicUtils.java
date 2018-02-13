package com.flipkart.vbroker.utils;

import com.flipkart.vbroker.core.TopicPartition;

import java.util.ArrayList;
import java.util.List;

public class TopicUtils {

    /**
     * Utility method to retrieve list of TopicPartition entities from given
     * parameters.
     *
     * @param topicId    topic id
     * @param partitions no of partitions
     * @return
     */
    public static List<TopicPartition> getTopicPartitions(short topicId, short partitions) {
        List<TopicPartition> topicPartitions = new ArrayList<>();
        for (short i = 0; i < partitions; i++) {
            topicPartitions.add(new TopicPartition(i, topicId));
        }
        return topicPartitions;
    }
}
