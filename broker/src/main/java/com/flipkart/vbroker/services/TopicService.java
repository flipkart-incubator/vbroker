package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.exceptions.TopicValidationException;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface TopicService {

    /**
     * Creates topic request. Called to submit topic creation request, which will be handled by controller.
     *
     * @param topic
     * @return CompletionStage with created topic as result
     */
    public CompletionStage<Topic> createTopic(Topic topic) throws TopicValidationException;

    /**
     * Creates topic entity. This is invoked by the controller to create actual topic entity.
     *
     * @param id
     * @param topic
     * @return
     * @throws TopicValidationException
     */
    public CompletionStage<Topic> createTopicAdmin(short id, Topic topic) throws TopicValidationException;

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
