package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.exceptions.TopicValidationException;
import com.flipkart.vbroker.utils.TopicUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryTopicService implements TopicService {

    private final ConcurrentMap<Short, Topic> topicsMap = new ConcurrentHashMap<>();

    @Override
    public synchronized CompletionStage<Topic> createTopic(Topic topic) throws TopicValidationException {
        return CompletableFuture.supplyAsync(() -> {
            topicsMap.putIfAbsent(topic.id(), topic);
            return topic;
        });
    }

    @Override
    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, short topicPartitionId) {
        return getTopic(topic.id())
            .thenApplyAsync(topic1 -> new TopicPartition(topicPartitionId, topic.id(), topic.grouped()));
    }

    @Override
    public CompletionStage<Boolean> isTopicPresent(short topicId) {
        return CompletableFuture.supplyAsync(() -> topicsMap.containsKey(topicId));
    }

    @Override
    public CompletionStage<Topic> getTopic(short topicId) {
        return CompletableFuture.supplyAsync(() -> topicsMap.get(topicId));
    }

    @Override
    public List<TopicPartition> getPartitions(Topic topic) {
        return TopicUtils.getTopicPartitions(topic);
    }

    @Override
    public CompletionStage<List<Topic>> getAllTopics() {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(topicsMap.values()));
    }
}
