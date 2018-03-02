package com.flipkart.vbroker.utils;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.wrappers.Topic;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TopicUtils {

    public static List<TopicPartition> getTopicPartitions(Topic topic) {
        return IntStream.range(0, topic.getProtoTopic().getPartitions())
            .mapToObj(i -> new TopicPartition(i, topic.getProtoTopic().getId(), topic.getProtoTopic().getGrouped()))
            .collect(Collectors.toList());
    }

    public static TopicPartition getTopicPartition(Topic topic, short partitionId) {
        if (partitionId >= topic.getProtoTopic().getPartitions()) {
            throw new VBrokerException(String.format("Partition with id %s not present for topic %s",
                partitionId, topic.getProtoTopic().getName()));
        }
        return new TopicPartition(partitionId, topic.getProtoTopic().getId(), topic.getProtoTopic().getGrouped());
    }

    public static Topic getTopic(byte[] bytes) {
        return Topic.fromBytes(bytes);
    }
}
