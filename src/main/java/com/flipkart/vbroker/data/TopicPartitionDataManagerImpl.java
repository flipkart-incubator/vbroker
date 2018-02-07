package com.flipkart.vbroker.data;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;

import java.util.Set;

public class TopicPartitionDataManagerImpl implements TopicPartitionDataManager {

    @Override
    public void addMessage(TopicPartition topicPartition, Message message) {

    }

    @Override
    public void addMessageGroup(TopicPartition topicPartition, MessageGroup messageGroup) {

    }

    @Override
    public Set<String> getUniqueGroups(TopicPartition topicPartition) {
        return null;
    }

    @Override
    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group) {
        return null;
    }

    @Override
    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group, int seqNoFrom) {
        return null;
    }
}
