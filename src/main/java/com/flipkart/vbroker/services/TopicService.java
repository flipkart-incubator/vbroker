package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;

import java.util.List;

public interface TopicService {

    public void createTopic(Topic topic);

    public void createTopicPartition(TopicPartition topicPartition);

    public TopicPartition getTopicPartition(short topicPartitionId);

    public Topic getTopic(int topicId);

    public List<TopicPartition> getPartitions(Topic topic);
}
