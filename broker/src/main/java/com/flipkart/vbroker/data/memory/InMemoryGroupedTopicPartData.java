package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.DataIterator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
public class InMemoryGroupedTopicPartData implements TopicPartData {
    private final ConcurrentMap<String, List<Message>> topicPartitionData = new ConcurrentHashMap<>();

    private final BlockingQueue<MessageWithFuture> incomingMsgQueue = new ArrayBlockingQueue<>(100000);
    private final CountDownLatch queueRunningLatch = new CountDownLatch(1);
    private volatile boolean isQueueRunning = true;

    InMemoryGroupedTopicPartData() {
        new Thread(() -> {
            MessageWithFuture messageWithFuture;
            while (isQueueRunning) {
                while (nonNull(messageWithFuture = incomingMsgQueue.peek())) {
                    Message message = messageWithFuture.getMessage();
                    log.debug("En-queued up message with msg_id {} and group_id {} to the map", message.messageId(), message.groupId());
                    try {
                        MessageMetadata messageMetadata = addMessageBlocking(message);
                        messageWithFuture.getFuture().complete(messageMetadata);
                    } catch (Throwable throwable) {
                        messageWithFuture.getFuture().completeExceptionally(throwable);
                    }
                    incomingMsgQueue.poll();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Error in sleeping", e);
                }
            }
            queueRunningLatch.countDown();
        }).start();
    }

    @Override
    public void close() throws Exception {
        isQueueRunning = false;
        queueRunningLatch.await();
        log.info("Closed {} instance successfully", this.getClass());
    }

    public CompletionStage<MessageMetadata> addMessage(Message message) {
        MessageWithFuture messageWithFuture = new MessageWithFuture(message, new CompletableFuture<>());
        try {
            boolean success = incomingMsgQueue.offer(messageWithFuture, 1, TimeUnit.SECONDS);
            if (!success) {
                messageWithFuture.getFuture().completeExceptionally(
                    new VBrokerException("Queue has reached capacity to en-queue message: " + message.messageId() + " with group: " + message.groupId()));
            }
        } catch (InterruptedException e) {
            messageWithFuture.getFuture().completeExceptionally(e);
        }
        log.debug("En-queued up message with msg_id {} and group_id {} to the map", message.messageId(), message.groupId());
        return messageWithFuture.getFuture();
    }

    private MessageMetadata addMessageBlocking(Message message) {
        getMessages(message.groupId()).add(message);
        log.debug("Added message with msg_id {} and group_id {} to the map", message.messageId(), message.groupId());
        return new MessageMetadata(message.messageId(),
            message.topicId(),
            message.partitionId(),
            new Random().nextInt());
    }

    public CompletionStage<Set<String>> getUniqueGroups() {
        return CompletableFuture.supplyAsync(topicPartitionData::keySet);
    }

    public DataIterator<Message> iteratorFrom(String group, int seqNoFrom) {
        if (log.isDebugEnabled()) {
            List<String> groupMessageIds = topicPartitionData.get(group)
                .stream()
                .map(Message::messageId)
                .collect(Collectors.toList());
            log.debug("Creating new iterator for group {} from seqNo {} having message_ids {}",
                group, seqNoFrom, groupMessageIds);
        }

        return new DataIterator<Message>() {
            AtomicInteger index = new AtomicInteger(seqNoFrom);

            @Override
            public String name() {
                return "Iterator_grouped_" + group + "_at_index_" + index;
            }

            @Override
            public Message peek() {
                Message message = topicPartitionData.get(group).get(index.get());
                if (log.isDebugEnabled()) {
                    log.debug("Group {} messages: {}", message.groupId(),
                        topicPartitionData.get(group).stream()
                            .map(Message::messageId).collect(Collectors.toList()));
                    log.debug("Peeking message {} with group {} at seqNo {}", message.messageId(), message.groupId(), index);
                }
                return message;
            }

            @Override
            public Message next() {
                Message message = topicPartitionData.get(group).get(index.getAndIncrement());
                log.trace("Next message {}", message.messageId());
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

    @Override
    public CompletionStage<Integer> getCurrentOffset(String group) {
        CompletableFuture<Integer> offsetFuture = new CompletableFuture<>();
        offsetFuture.complete(this.topicPartitionData.get(group).size());
        return offsetFuture;
    }

    @Override
    public DataIterator<Message> iteratorFrom(int seqNoFrom) {
        throw new UnsupportedOperationException("You cannot have a global iterator for partition for a grouped topic-partition");
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset() {
        throw new UnsupportedOperationException("Global offset is not defined for grouped topic-partition");
    }

    private List<Message> getMessages(String group) {
        return topicPartitionData.computeIfAbsent(group, group1 -> new ArrayList<>());
    }

    @AllArgsConstructor
    @Getter
    private class MessageWithFuture {
        private final Message message;
        private final CompletableFuture<MessageMetadata> future;
    }
}
