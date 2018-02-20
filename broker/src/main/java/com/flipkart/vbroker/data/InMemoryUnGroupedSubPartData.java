package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.flipkart.vbroker.iterators.UnGroupedFailedMessageIterator;
import com.flipkart.vbroker.subscribers.MessageWithMetadata;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.flipkart.vbroker.subscribers.UnGroupedMessageWithMetadata;
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
    private final Map<QType, List<MessageWithMetadata>> failedMessagesMap = new ConcurrentHashMap<>();

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

    private CompletionStage<List<MessageWithMetadata>> getFailedMessages(QType qType) {
        return CompletableFuture.supplyAsync(() -> {
            failedMessagesMap.computeIfAbsent(qType, qType1 -> new LinkedList<>());
            return failedMessagesMap.get(qType);
        });
    }

    @Override
    public CompletionStage<Void> sideline(MessageWithMetadata messageWithMetadata) {
        return getFailedMessages(messageWithMetadata.getQType())
            .thenAccept(messages -> messages.add(messageWithMetadata));
    }

    @Override
    public CompletionStage<Void> retry(MessageWithMetadata messageWithMetadata) {
        return getFailedMessages(messageWithMetadata.getQType())
            .thenAccept(messages -> messages.add(messageWithMetadata));
    }

    @Override
    public PeekingIterator<MessageWithMetadata> getIterator(String groupId) {
        throw new UnsupportedOperationException("You cannot get groupId level iterator for a un-grouped subscription");
    }

    @Override
    public Optional<PeekingIterator<MessageWithMetadata>> getIterator(QType qType) {
        PartSubscriberIterator partSubscriberIterator = new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<MessageWithMetadata>> nextIterator() {
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
                            protected MessageWithMetadata getMessageWithMetadata(int indexNo) {
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

    private Optional<PeekingIterator<MessageWithMetadata>> getIterator(PeekingIterator<Message> iterator) {
        PeekingIterator<MessageWithMetadata> peekingIterator = new PeekingIterator<MessageWithMetadata>() {
            @Override
            public MessageWithMetadata peek() {
                return new UnGroupedMessageWithMetadata(iterator.peek(), partSubscription);
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public synchronized MessageWithMetadata next() {
                MessageWithMetadata messageWithGroup = new UnGroupedMessageWithMetadata(iterator.next(), partSubscription);
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

    @Override
    public CompletionStage<Integer> getCurSeqNo(String groupId) {
        throw new UnsupportedOperationException("SeqNo is not defined at group level for ungrouped subscription");
    }

    @Override
    public CompletionStage<Integer> getLag() {
        return null;
    }
}
