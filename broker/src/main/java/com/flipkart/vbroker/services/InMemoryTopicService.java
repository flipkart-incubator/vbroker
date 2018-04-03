package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.exceptions.TopicValidationException;
import com.flipkart.vbroker.utils.TopicUtils;
import com.flipkart.vbroker.wrappers.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class InMemoryTopicService implements TopicService {

    private final ExecutorService executorService;
    private final ConcurrentMap<Integer, Topic> topicsMap = new ConcurrentHashMap<>();

    public InMemoryTopicService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public synchronized CompletionStage<Topic> createTopic(Topic topic) throws TopicValidationException {
        return CompletableFuture.supplyAsync(() -> {
            topicsMap.putIfAbsent(topic.id(), topic);
            return topic;
        }, executorService);
    }

    @Override
    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, int topicPartitionId) {
        return getTopic(topic.id())
            .thenApplyAsync(topic1 -> new TopicPartition(topicPartitionId, topic.id(), topic.grouped()), executorService);
    }

    @Override
    public CompletionStage<Boolean> isTopicPresent(int topicId) {
        return CompletableFuture.supplyAsync(() -> topicsMap.containsKey(topicId), executorService);
    }

    @Override
    public CompletionStage<Topic> getTopic(int topicId) {
        return CompletableFuture.supplyAsync(() -> topicsMap.get(topicId), executorService);
    }

    @Override
    public List<TopicPartition> getPartitions(Topic topic) {
        return TopicUtils.getTopicPartitions(topic);
    }

    @Override
    public CompletionStage<List<Topic>> getAllTopics() {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(topicsMap.values()), executorService);
    }

    @Override
    public CompletionStage<Topic> createTopicAdmin(int id, Topic topic) throws TopicValidationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletionStage<Boolean> isTopicPresent(String name) {
        // TODO Auto-generated method stub
        return null;
    }
}
