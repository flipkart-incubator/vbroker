package com.flipkart.vbroker.data;

import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import java.util.*;

public class InMemoryTopicPartData implements TopicPartData {
    private final Map<String, List<Message>> topicPartitionData = new LinkedHashMap<>();

    public synchronized void addMessage(Message message) {
        this.topicPartitionData.computeIfAbsent(message.groupId(), key -> new LinkedList<>());
        this.topicPartitionData.get(message.groupId()).add(message);
    }

    public Set<String> getUniqueGroups() {
        return topicPartitionData.keySet();
    }

    public PeekingIterator<Message> iteratorFrom(String group, int seqNoFrom) {
        return Iterators.peekingIterator(topicPartitionData.get(group).listIterator(seqNoFrom));
    }
}
