package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import com.flipkart.vbroker.utils.TopicUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.common.collect.*;

import java.util.List;
import java.util.Map;

public class MetadataImpl implements Metadata {

    private final List<Topic> topics = Lists.newArrayList();
    private final Map<Integer, Topic> topicsMap = Maps.newConcurrentMap();

    private final List<Subscription> subscriptions = Lists.newArrayList();
    private final Table<Integer, Integer, Subscription> subscriptionTable = HashBasedTable.create();

    private final List<Node> nodes = Lists.newArrayList();
    private final Multimap<Node, TopicPartition> nodeTopicPartMap = HashMultimap.create();

    private final long createTimeMs;

    public MetadataImpl(VBClientConfig config) {
        this.createTimeMs = System.currentTimeMillis();

        topics.add(DummyEntities.groupedTopic);
        topics.add(DummyEntities.unGroupedTopic);
        topics.forEach(topic -> topicsMap.put(topic.id(), topic));

        subscriptions.add(DummyEntities.groupedSubscription);
        subscriptions.add(DummyEntities.unGroupedSubscription);
        subscriptions.forEach(subscription ->
            subscriptionTable.put(subscription.topicId(), subscription.id(), subscription));

        Node node = new Node(0, config.getBrokerHost(), config.getBrokerPort());
        nodes.add(node);

        TopicUtils.getTopicPartitions(DummyEntities.groupedTopic)
            .forEach(topicPartition -> nodeTopicPartMap.put(node, topicPartition));
    }

    @Override
    public long aliveTimeMs() {
        return (System.currentTimeMillis() - createTimeMs);
    }

    @Override
    public List<Topic> getTopics() {
        return topics;
    }

    @Override
    public Topic getTopic(int topicId) {
        return topicsMap.get(topicId);
    }

    @Override
    public TopicPartition getTopicPartition(int topicId, int partitionId) {
        Topic topic = getTopic(topicId);
        return TopicUtils.getTopicPartition(topic, partitionId);
    }

    @Override
    public List<TopicPartition> getTopicPartitions(int topicId) {
        Topic topic = getTopic(topicId);
        return TopicUtils.getTopicPartitions(topic);
    }

    @Override
    public List<TopicPartition> getTopicPartitions(Node node) {
        return Lists.newArrayList(nodeTopicPartMap.get(node));
    }

    @Override
    public List<Node> getClusterNodes() {
        return nodes;
    }

    @Override
    public Node getLeaderNode(TopicPartition topicPartition) {
        return nodes.get(0);
    }

    @Override
    public List<PartSubscription> getPartSubscriptions(int topicId, int subscriptionId) {
        Subscription subscription = subscriptionTable.get(topicId, subscriptionId);
        List<TopicPartition> topicPartitions = getTopicPartitions(topicId);
        return SubscriptionUtils.getPartSubscriptions(subscription, topicPartitions.size());
    }
}
