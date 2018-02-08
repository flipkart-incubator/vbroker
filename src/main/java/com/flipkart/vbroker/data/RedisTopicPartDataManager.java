package com.flipkart.vbroker.data;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class RedisTopicPartDataManager implements TopicPartDataManager {

    private final Map<TopicPartition, TopicPartData> allPartitionsDataMap = new LinkedHashMap<>();
    private Config config = new Config();
    private RedissonClient client;


    public RedisTopicPartDataManager() {
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        client = Redisson.create(config);
    }

    @Override
    public TopicPartData getTopicPartData(TopicPartition topicPartition) {
        allPartitionsDataMap.putIfAbsent(topicPartition, new RedisTopicPartData(topicPartition));
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
