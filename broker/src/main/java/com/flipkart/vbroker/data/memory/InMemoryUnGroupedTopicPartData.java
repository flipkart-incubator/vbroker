package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.VIterator;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class InMemoryUnGroupedTopicPartData implements TopicPartData {

    //private BlockingQueue<Message> messageQueue = new ArrayBlockingQueue<>(10000);
    private List<Message> messages = new ArrayList<>();//CopyOnWriteArrayList<>();

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
    public VIterator<Message> iteratorFrom(int seqNoFrom) {
        log.info("Creating new iterator for {}", this.getClass());
        return new VIterator<Message>() {

            AtomicInteger index = new AtomicInteger(seqNoFrom);

            @Override
            public String name() {
                //log.info("Messages {}", messages);
                return "Iterator_un_grouped_with_index_" + index.get() + "_" + this.hashCode();
            }

            @Override
            public Message peek() {
                Message message = messages.get(index.get());
                log.info("Peeking message {} at seqNo {}", message.messageId(), index.get());
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
                int messagesSize = messages.size();
                log.debug("Idx is at {} and no of messages are {}", index.get(), messagesSize);
                return index.get() < messagesSize;
            }
        };
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset() {
        return CompletableFuture.supplyAsync(() -> messages.size());
    }
}
