package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface TopicService {

    public CompletionStage<Topic> createTopic(Topic topic);

    public void createTopicPartition(Topic topic, TopicPartition topicPartition);

    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, short topicPartitionId);

    public CompletionStage<Topic> getTopic(short topicId);

    public List<TopicPartition> getPartitions(Topic topic);

    public CompletionStage<List<Topic>> getAllTopics();
}
