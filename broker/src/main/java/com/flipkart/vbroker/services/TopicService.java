package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface TopicService {

    public CompletionStage<Topic> createTopic(Topic topic);

    // public void createTopicPartition(Topic topic, TopicPartition topicPartition);

    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, short topicPartitionId);

    /**
     * check if the topic is present
     *
     * @param topicId of the topic to check
     * @return async true if present and false otherwise
     */
    public CompletionStage<Boolean> isTopicPresent(short topicId);

    public CompletionStage<Topic> getTopic(short topicId);

    public List<TopicPartition> getPartitions(Topic topic);

    public CompletionStage<List<Topic>> getAllTopics();
}
