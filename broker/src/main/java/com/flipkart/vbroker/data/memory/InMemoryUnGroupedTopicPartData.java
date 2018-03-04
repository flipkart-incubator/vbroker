package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.flatbuf.Message;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class InMemoryUnGroupedTopicPartData implements TopicPartData {

    //private BlockingQueue<Message> messageQueue = new ArrayBlockingQueue<>(10000);
    private List<Message> messages = new CopyOnWriteArrayList<>();

    @Override
    public CompletionStage<MessageMetadata> addMessage(Message message) {
        return CompletableFuture.supplyAsync(() -> {
            messages.add(message);
            return new MessageMetadata(
                message.messageId(),
                message.topicId(),
                message.partitionId(),
                new Random().nextInt());
        });
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {
        throw new UnsupportedOperationException("For an un-grouped queue, you cannot list unique groups");
    }

    @Override
    public PeekingIterator<Message> iteratorFrom(String group, int seqNoFrom) {
        throw new UnsupportedOperationException("For an un-grouped queue, you cannot have a group level iterator");
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset(String group) {
        throw new UnsupportedOperationException("For an un-grouped queue, you cannot get group level offset");
    }

    @Override
    public PeekingIterator<Message> iteratorFrom(int seqNoFrom) {
        return new PeekingIterator<Message>() {
            AtomicInteger index = new AtomicInteger(seqNoFrom);

            @Override
            public Message peek() {
                Message message = messages.get(index.get());
                log.trace("Peeking message {}", message.messageId());
                return message;
            }

            @Override
            public Message next() {
                Message message = messages.get(index.getAndIncrement());
                log.trace("Next message {}", message.messageId());
                return message;
            }

            @Override
            public void remove() {
                throw new NotImplementedException();
            }

            @Override
            public boolean hasNext() {
                return index.get() < messages.size();
            }
        };
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset() {
        return CompletableFuture.supplyAsync(() -> messages.size());
    }
}
