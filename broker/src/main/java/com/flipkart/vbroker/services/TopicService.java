package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.exceptions.TopicValidationException;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface TopicService {

    /**
     * Creates topic entity. Called by controller to create a valid topic.
     *
     * @param topic
     * @return CompletionStage with created topic as result
     */
    public CompletionStage<Topic> createTopic(Topic topic) throws TopicValidationException;

    public CompletionStage<Topic> createTopicAdmin(Topic topic) throws TopicValidationException;

    // public void createTopicPartition(Topic topic, TopicPartition topicPartition);

    /**
     * Gets topic partition.
     *
     * @param topic            topic to get partition for
     * @param topicPartitionId partitionId of partition to be fetched
     * @return
     */
    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, short topicPartitionId);

    /**
     * check if the topic is present
     *
     * @param topicId of the topic to check
     * @return async true if present and false otherwise
     */
    public CompletionStage<Boolean> isTopicPresent(short topicId);

    /**
     * Get topic with specified id.
     *
     * @param topicId id of the topic
     * @return CompletionStage with topic as result.
     */
    public CompletionStage<Topic> getTopic(short topicId);

    /**
     * Get topic partitions list for the topic
     *
     * @param topic Topic to get partitions for
     * @return
     */
    public List<TopicPartition> getPartitions(Topic topic);

    /**
     * Get all topics
     *
     * @return CompletionStage with list of topics as result
     */
    public CompletionStage<List<Topic>> getAllTopics();
}
