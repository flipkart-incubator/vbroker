package com.flipkart.vbroker.data;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class InMemoryTopicPartitionDataManager implements TopicPartitionDataManager {

    private final Map<TopicPartition, TopicPartData> allPartitionsDataMap = new LinkedHashMap<>();

    @Override
    public TopicPartData getTopicPartData(TopicPartition topicPartition) {
        allPartitionsDataMap.putIfAbsent(topicPartition, new InMemoryTopicPartData());
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
