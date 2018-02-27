package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.flipkart.vbroker.subscribers.UnGroupedIterableMessage;
import com.google.common.collect.PeekingIterator;
import lombok.AllArgsConstructor;
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

    private final Map<QType, AtomicInteger> currSeqNoMap = new ConcurrentHashMap<>();
    private final Map<QType, List<IterableMessage>> failedMessagesMap = new ConcurrentHashMap<>();

    public InMemoryUnGroupedSubPartData(PartSubscription partSubscription,
                                        TopicPartDataManager topicPartDataManager) {
        log.info("Creating InMemoryUnGroupedSubPartDat obj");
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
    public synchronized Optional<PeekingIterator<IterableMessage>> getIterator(QType qType) {
        PartSubscriberIterator partSubscriberIterator = new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<IterableMessage>> nextIterator() {
                PeekingIterator<Message> iterator;
                switch (qType) {
                    case MAIN:
                        iterator = topicPartDataManager.getIterator(
                            partSubscription.getTopicPartition(),
                            getCurrSeqNoFor(qType));
                        break;
                    default:
                        iterator = new UnGroupedFailedMsgIterator(qType);
                        break;
                }
                UnGroupedIterator unGroupedIterator =
                    new UnGroupedIterator(qType, partSubscription, iterator);
                return Optional.of(unGroupedIterator);
            }
        };
        return Optional.of(partSubscriberIterator);
    }

    private int getCurrSeqNoFor(QType qType) {
        return currSeqNoMap.computeIfAbsent(qType, qType1 -> new AtomicInteger(0)).get();
    }

    private void incrementCurrSeqNo(QType qType) {
        currSeqNoMap.computeIfAbsent(qType, qType1 -> new AtomicInteger(0)).incrementAndGet();
    }

    private IterableMessage getMessage(QType qType, int seqNo) {
        return getFailedMessages(qType).thenApply(iterableMessages -> iterableMessages.get(seqNo))
            .toCompletableFuture().join();
    }

    @AllArgsConstructor
    class UnGroupedIterator implements PeekingIterator<IterableMessage> {

        private final QType qType;
        private final PartSubscription partSubscription;
        private final PeekingIterator<Message> iterator;

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
            IterableMessage iterableMessage = new UnGroupedIterableMessage(iterator.next(), partSubscription);
            incrementCurrSeqNo(qType);
            return iterableMessage;
        }

        @Override
        public void remove() {
            throw new NotImplementedException();
        }
    }

    @AllArgsConstructor
    class UnGroupedFailedMsgIterator implements PeekingIterator<Message> {
        private final QType qType;

        @Override
        public Message peek() {
            return getIterableMessage(getCurrSeqNo()).getMessage();
        }

        private IterableMessage getIterableMessage(int indexNo) {
            return getMessage(qType, indexNo);
        }

        @Override
        public Message next() {
            return getIterableMessage(getCurrSeqNo()).getMessage();
        }

        @Override
        public void remove() {
            throw new NotImplementedException("Remove not supported for iterator");
        }

        private int getCurrSeqNo() {
            return getCurrSeqNoFor(qType);
        }

        private int getTotalSize() {
            return getFailedMessages(qType).toCompletableFuture().join().size();
        }

        @Override
        public boolean hasNext() {
            int totalSize = getTotalSize();
            int currIndex = getCurrSeqNo();
            //log.debug("Total failed messages are {} and currIdx is {}", totalSize, currIndex);
            return currIndex < totalSize;
        }
    }
}
