package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.exceptions.TopicNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryTopicService implements TopicService {

    private final ConcurrentMap<Short, Topic> topicsMap = new ConcurrentHashMap<>();

    @Override
    public synchronized CompletionStage<Topic> createTopic(Topic topic) {
        return CompletableFuture.supplyAsync(() -> {
            topicsMap.putIfAbsent(topic.getId(), topic);
            return topic;
        });
    }

    @Override
    public synchronized void createTopicPartition(Topic topic, TopicPartition topicPartition) {
        topic.addPartition(topicPartition);
        topicsMap.putIfAbsent(topic.getId(), topic);
    }

    @Override
    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, short topicPartitionId) {
        if (!topicsMap.containsKey(topic.getId())) {
            throw new TopicNotFoundException("Not found topic with id: " + topic.getId());
        }
        return CompletableFuture.supplyAsync(() -> {
            Topic existingTopic = topicsMap.get(topic.getId());
            return existingTopic.getPartition(topicPartitionId);
        });
    }

    @Override
    public CompletionStage<Topic> getTopic(short topicId) {
        return CompletableFuture.supplyAsync(() -> topicsMap.get(topicId));
    }


    @Override
    public List<TopicPartition> getPartitions(Topic topic) {
        return topic.getPartitions();
    }

    @Override
    public CompletionStage<List<Topic>> getAllTopics() {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(topicsMap.values()));
    }
}
