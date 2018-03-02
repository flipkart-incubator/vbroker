package com.flipkart.vbroker.utils;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.wrappers.Topic;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TopicUtils {

    public static List<TopicPartition> getTopicPartitions(Topic topic) {
        return IntStream.range(0, topic.partitions())
            .mapToObj(i -> new TopicPartition(i, topic.id(), topic.grouped()))
            .collect(Collectors.toList());
    }

    public static TopicPartition getTopicPartition(Topic topic, int partitionId) {
        if (partitionId >= topic.partitions()) {
            throw new VBrokerException(String.format("Partition with id %s not present for topic %s",
                partitionId, topic.name()));
        }
        return new TopicPartition(partitionId, topic.id(), topic.grouped());
    }

    public static Topic getTopic(byte[] bytes) {
        return Topic.fromBytes(bytes);
    }
}
