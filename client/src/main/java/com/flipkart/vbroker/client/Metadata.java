package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.wrappers.Topic;

import java.util.List;

public interface Metadata {

    public long aliveTimeMs();

    public Topic getTopic(int topicId);

    public List<Topic> getTopics();

    public TopicPartition getTopicPartition(int topicId, int partitionId);

    public List<TopicPartition> getTopicPartitions(int topicId);

    public List<TopicPartition> getTopicPartitions(Node node);

    public List<Node> getClusterNodes();

    public Node getLeaderNode(TopicPartition topicPartition);

    //subscriptions
    public List<PartSubscription> getPartSubscriptions(int topicId, int subscriptionId);
}
