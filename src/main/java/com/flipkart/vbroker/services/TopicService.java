package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;

import java.util.List;

public interface TopicService {

    public void createTopic(Topic topic);

    public void createTopicPartition(Topic topic, TopicPartition topicPartition);

    public TopicPartition getTopicPartition(Topic topic, short topicPartitionId);

    public Topic getTopic(short topicId);

    public List<TopicPartition> getPartitions(Topic topic);
}