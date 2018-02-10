package com.flipkart.vbroker.data;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RedisTopicPartDataManager implements TopicPartDataManager {

    private static final Config messageCodecConfig = new Config();
    private static final Config defaultCodecConfig = new Config();
    private static RedissonClient messageCodecClient;
    private static RedissonClient defaultCodecClient;
    private static Codec redisCodec = new RedisMessageCodec();
    private final Map<TopicPartition, TopicPartData> allPartitionsDataMap = new LinkedHashMap<>();

    public RedisTopicPartDataManager(VBrokerConfig brokerConfig) {
        messageCodecConfig.useSingleServer().setAddress(brokerConfig.getRedisUrl());
        defaultCodecConfig.useSingleServer().setAddress(brokerConfig.getRedisUrl());
        messageCodecConfig.setCodec(redisCodec);
        this.messageCodecClient = Redisson.create(messageCodecConfig);
        this.defaultCodecClient = Redisson.create(defaultCodecConfig);
    }

    @Override
    public TopicPartData getTopicPartData(TopicPartition topicPartition) {
        allPartitionsDataMap.putIfAbsent(topicPartition, new RedisTopicPartData(messageCodecClient, defaultCodecClient, topicPartition));
        return allPartitionsDataMap.get(topicPartition);
    }

    @Override
    public void addMessage(TopicPartition topicPartition, Message message) {
        TopicPartData topicPartData = getTopicPartData(topicPartition);
        topicPartData.addMessage(message);
    }

    @Override
    public void addMessageGroup(TopicPartition topicPartition, MessageGroup messageGroup) {
        //
    }

    @Override
    public Set<String> getUniqueGroups(TopicPartition topicPartition) {
        TopicPartData topicPartData = getTopicPartData(topicPartition);
        return topicPartData.getUniqueGroups();
    }

    @Override
    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group) {
        return null;
    }

    @Override
    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group, int seqNoFrom) {
        TopicPartData topicPartData = getTopicPartData(topicPartition);
        return topicPartData.iteratorFrom(group, seqNoFrom);
    }
}
