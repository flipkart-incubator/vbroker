package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.flipkart.vbroker.iterators.UnGroupedFailedMessageIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.flipkart.vbroker.subscribers.UnGroupedIterableMessage;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class InMemoryUnGroupedSubPartData implements SubPartData {

    private final PartSubscription partSubscription;
    private final TopicPartDataManager topicPartDataManager;

    private final AtomicInteger currSeqNo = new AtomicInteger(0);
    private final Map<QType, List<IterableMessage>> failedMessagesMap = new ConcurrentHashMap<>();

    public InMemoryUnGroupedSubPartData(PartSubscription partSubscription,
                                        TopicPartDataManager topicPartDataManager) {
        this.partSubscription = partSubscription;
        this.topicPartDataManager = topicPartDataManager;
    }

    @Override
    public CompletionStage<MessageMetadata> addGroup(SubscriberGroup subscriberGroup) {
        throw new UnsupportedOperationException("You cannot add SubscriberGroup to an un-grouped subscription");
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {
        throw new UnsupportedOperationException("You cannot get unique groups to an un-grouped subscription");
    }

    private CompletionStage<List<IterableMessage>> getFailedMessages(QType qType) {
        return CompletableFuture.supplyAsync(() -> {
            failedMessagesMap.computeIfAbsent(qType, qType1 -> new LinkedList<>());
            return failedMessagesMap.get(qType);
        });
    }

    @Override
    public CompletionStage<Void> sideline(IterableMessage iterableMessage) {
        return getFailedMessages(iterableMessage.getQType())
            .thenAccept(messages -> messages.add(iterableMessage));
    }

    @Override
    public CompletionStage<Void> retry(IterableMessage iterableMessage) {
        return getFailedMessages(iterableMessage.getQType())
            .thenAccept(messages -> messages.add(iterableMessage));
    }

    @Override
    public PeekingIterator<IterableMessage> getIterator(String groupId) {
        throw new UnsupportedOperationException("You cannot get groupId level iterator for a un-grouped subscription");
    }

    @Override
    public Optional<PeekingIterator<IterableMessage>> getIterator(QType qType) {
        PartSubscriberIterator partSubscriberIterator = new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<IterableMessage>> nextIterator() {
                PeekingIterator<Message> iterator;

                switch (qType) {
                    case MAIN:
                        iterator = topicPartDataManager.getIterator(
                            partSubscription.getTopicPartition(),
                            currSeqNo.get()
                        );
                        break;
                    default:
                        iterator = new UnGroupedFailedMessageIterator() {
                            @Override
                            protected int getTotalSize() {
                                return failedMessagesMap.get(qType).size();
                            }

                            @Override
                            protected IterableMessage getMessageWithMetadata(int indexNo) {
                                return failedMessagesMap.get(qType).get(indexNo);
                            }
                        };
                        break;
                }

                return getIterator(iterator);
            }
        };

        return Optional.of(partSubscriberIterator);
    }

    private Optional<PeekingIterator<IterableMessage>> getIterator(PeekingIterator<Message> iterator) {
        PeekingIterator<IterableMessage> peekingIterator = new PeekingIterator<IterableMessage>() {
            @Override
            public IterableMessage peek() {
                return new UnGroupedIterableMessage(iterator.peek(), partSubscription);
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public synchronized IterableMessage next() {
                IterableMessage messageWithGroup = new UnGroupedIterableMessage(iterator.next(), partSubscription);
                currSeqNo.incrementAndGet();
                return messageWithGroup;
            }

            @Override
            public void remove() {
                throw new NotImplementedException();
            }
        };

        return Optional.of(peekingIterator);
    }
}
