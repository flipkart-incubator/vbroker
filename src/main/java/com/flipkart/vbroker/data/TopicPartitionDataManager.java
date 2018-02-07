package com.flipkart.vbroker.data;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.PeekingIterator;

import java.util.Set;

public interface TopicPartitionDataManager {

    public void addMessage(TopicPartition topicPartition, Message message);

    public void addMessageGroup(TopicPartition topicPartition, MessageGroup messageGroup);

    public Set<String> getUniqueGroups(TopicPartition topicPartition);

    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group);

    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group, int seqNoFrom);
}