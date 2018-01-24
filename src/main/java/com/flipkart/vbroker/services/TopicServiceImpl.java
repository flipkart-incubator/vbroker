package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.exceptions.VBrokerException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TopicServiceImpl implements TopicService {

    private final ConcurrentMap<Short, Topic> topicsMap = new ConcurrentHashMap<>();

    @Override
    public synchronized void createTopic(Topic topic) {
        topicsMap.putIfAbsent(topic.getId(), topic);
    }

    @Override
    public synchronized void createTopicPartition(Topic topic, TopicPartition topicPartition) {
        topic.addPartition(topicPartition);
        topicsMap.putIfAbsent(topic.getId(), topic);
    }

    @Override
    public TopicPartition getTopicPartition(Topic topic, short topicPartitionId) {
        if (!topicsMap.containsKey(topic.getId())) {
            throw new VBrokerException("Not found topic with id: " + topic.getId());
        }
        Topic existingTopic = topicsMap.get(topic.getId());
        return existingTopic.getPartition(topicPartitionId);
    }

    @Override
    public Topic getTopic(short topicId) {
        return topicsMap.get(topicId);
    }

    @Override
    public List<TopicPartition> getPartitions(Topic topic) {
        return topic.getPartitions();
    }
}
