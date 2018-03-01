package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.subscribers.DummyEntities;
import com.flipkart.vbroker.utils.TopicUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

public class MetadataImpl implements Metadata {

    private final List<Topic> topics = Lists.newArrayList();
    private final Map<Short, Topic> topicsMap = Maps.newConcurrentMap();

    private final List<Node> nodes = Lists.newArrayList();
    private final Multimap<Node, TopicPartition> nodeTopicPartMap = HashMultimap.create();

    private final long createTimeMs;

    public MetadataImpl(VBClientConfig config) {
        this.createTimeMs = System.currentTimeMillis();

        topics.add(DummyEntities.groupedTopic);
        topics.add(DummyEntities.unGroupedTopic);

        topics.stream()
            .map(topic -> topicsMap.putIfAbsent(topic.id(), topic));

        Node node = new Node(0, config.getBrokerHost(), config.getBrokerPort());
        nodes.add(node);

        TopicUtils.getTopicPartitions(DummyEntities.groupedTopic)
            .stream()
            .map(topicPartition -> nodeTopicPartMap.put(node, topicPartition));
    }

    @Override
    public long aliveTimeMs() {
        return (System.currentTimeMillis() - createTimeMs);
    }

    @Override
    public List<Topic> getTopics() {
        return topics;
    }

    private Topic getTopic(short topicId) {
        return topics.get(topicId);
    }

    @Override
    public TopicPartition getTopicPartition(short topicId, short partitionId) {
        Topic topic = getTopic(topicId);
        return TopicUtils.getTopicPartition(topic, partitionId);
    }

    @Override
    public List<TopicPartition> getTopicPartitions(short topicId) {
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
}
