package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.Objects.nonNull;

@Slf4j
public class InMemoryUngroupedTopicPartData implements TopicPartData {

    private BlockingQueue<Message> messageQueue = new ArrayBlockingQueue<>(10000);

    @Override
    public CompletionStage<MessageMetadata> addMessage(Message message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                messageQueue.put(message);
            } catch (InterruptedException e) {
                throw new VBrokerException(e);
            }
            return new MessageMetadata(message.topicId(), message.partitionId(), new Random().nextInt());
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
    public PeekingIterator<Message> iteratorFrom(int seqNoFrom) {
        return new PeekingIterator<Message>() {

            @Override
            public Message peek() {
                Message message = messageQueue.peek();
                log.trace("Peeking message {}", message.messageId());
                return message;
            }

            @Override
            public Message next() {
                Message message = messageQueue.poll();
                log.trace("Next message {}", message.messageId());
                return message;
            }

            @Override
            public void remove() {
                throw new NotImplementedException();
            }

            @Override
            public boolean hasNext() {
                return nonNull(messageQueue.peek());
            }
        };
    }
}
