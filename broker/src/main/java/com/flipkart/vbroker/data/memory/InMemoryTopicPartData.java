package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
public class InMemoryTopicPartData implements TopicPartData {
    private final Map<String, List<Message>> topicPartitionData = new LinkedHashMap<>();

    public synchronized CompletionStage<MessageMetadata> addMessage(Message message) {
        return CompletableFuture.supplyAsync(() -> {
            getMessages(message.groupId()).add(message);
            log.info("Added message with msg_id {} and group_id {} to the map", message.messageId(), message.groupId());
            log.info("Group messages: {}", topicPartitionData.get(message.groupId()));
            return new MessageMetadata(message.topicId(), message.partitionId(), new Random().nextInt());
        });
    }

    public CompletionStage<Set<String>> getUniqueGroups() {
        return CompletableFuture.supplyAsync(topicPartitionData::keySet);
    }

    public PeekingIterator<Message> iteratorFrom(String group, int seqNoFrom) {

        return new PeekingIterator<Message>() {
            AtomicInteger index = new AtomicInteger(seqNoFrom);

            @Override
            public Message peek() {
                Message message = topicPartitionData.get(group).get(index.get());
                log.info("Peeking message {}", message.messageId());
                return message;
            }

            @Override
            public Message next() {
                Message message = topicPartitionData.get(group).get(index.getAndIncrement());
                log.info("Next message {}", message.messageId());
                return message;
            }

            @Override
            public void remove() {
                throw new NotImplementedException();
            }

            @Override
            public boolean hasNext() {
                return index.get() < topicPartitionData.get(group).size();
            }
        };
    }

    public Stream<Message> streamFrom(String group, int seqNoFrom) {
        return getMessages(group).stream().skip(seqNoFrom);
    }

    private synchronized List<Message> getMessages(String group) {
        return topicPartitionData.computeIfAbsent(group, key -> new CopyOnWriteArrayList<>());
    }
}
