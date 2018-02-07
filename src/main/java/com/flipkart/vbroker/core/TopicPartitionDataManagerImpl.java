package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;

import java.util.Optional;
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
    public PeekingIterator<MessageGroup> getMessageGroupIterator(TopicPartition topicPartition) {
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

    @Override
    public Optional<MessageGroup> getMessageGroup(TopicPartition topicPartition, String group) {
        return Optional.empty();
    }
}
