package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.exceptions.TopicNotFoundException;
import com.flipkart.vbroker.utils.TopicUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryTopicService implements TopicService {

    private final ConcurrentMap<Short, Topic> topicsMap = new ConcurrentHashMap<>();

    @Override
    public synchronized void createTopic(Topic topic) {
        topicsMap.putIfAbsent(topic.topicId(), topic);
    }

//    @Override
//    public synchronized void createTopicPartition(Topic topic, TopicPartition topicPartition) {
//        //topic.addPartition(topicPartition);
//        topicsMap.putIfAbsent(topic.topicId(), topic);
//    }

    @Override
    public TopicPartition getTopicPartition(Topic topic, short topicPartitionId) {
        if (!topicsMap.containsKey(topic.topicId())) {
            throw new TopicNotFoundException("Not found topic with id: " + topic.topicId());
        }
        Topic existingTopic = topicsMap.get(topic.topicId());
        //return existingTopic.getPartition(topicPartitionId);
        return new TopicPartition(topicPartitionId, existingTopic.topicId());
    }

    @Override
    public Topic getTopic(short topicId) {
        return topicsMap.get(topicId);
    }

    @Override
    public List<TopicPartition> getPartitions(Topic topic) {
        //return topic.getPartitions();
    	return TopicUtils.getTopicPartitions(topic.topicId(), topic.partitions());
    }

    @Override
    public List<Topic> getAllTopics() {
        return new ArrayList<>(topicsMap.values());
    }
}
