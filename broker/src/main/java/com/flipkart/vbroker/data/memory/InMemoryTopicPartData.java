package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class InMemoryTopicPartData implements TopicPartData {
    private final Map<String, List<Message>> topicPartitionData = new LinkedHashMap<>();

    public synchronized CompletionStage<MessageMetadata> addMessage(Message message) {
        return CompletableFuture.supplyAsync(() -> {
            this.topicPartitionData.computeIfAbsent(message.groupId(), key -> new CopyOnWriteArrayList<>());
            this.topicPartitionData.get(message.groupId()).add(message);
            return new MessageMetadata(message.topicId(), message.partitionId(), new Random().nextInt());
        });
    }

    public CompletionStage<Set<String>> getUniqueGroups() {
        return CompletableFuture.supplyAsync(topicPartitionData::keySet);
    }

    public PeekingIterator<Message> iteratorFrom(String group, int seqNoFrom) {
        List<Message> messages = topicPartitionData.get(group);
        return Iterators.peekingIterator(messages.listIterator(seqNoFrom));
    }
}
