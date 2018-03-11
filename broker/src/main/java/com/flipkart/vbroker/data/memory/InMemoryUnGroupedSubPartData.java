package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartData;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.DataIterator;
import com.flipkart.vbroker.iterators.MsgIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.flipkart.vbroker.subscribers.UnGroupedIterableMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public DataIterator<IterableMessage> getIterator(String groupId) {
        throw new UnsupportedOperationException("You cannot get groupId level iterator for a un-grouped subscription");
    }

    @Override
    public synchronized DataIterator<IterableMessage> getIterator(QType qType) {
        log.info("Creating new iterator for QType {}", qType);
        if (QType.MAIN.equals(qType)) {
            DataIterator<Message> msgIterator = topicPartDataManager.getIterator(
                partSubscription.getTopicPartition(),
                getCurrSeqNoFor(qType));
            return newUnGroupedIterator(qType, msgIterator);
        }

        DataIterator<Message> iterator = new UnGroupedFailedMsgIterator(qType);
        return newUnGroupedIterator(qType, iterator);
    }

    private UnGroupedIterator newUnGroupedIterator(QType qType, DataIterator<Message> msgIterator) {
        return new UnGroupedIterator(qType, partSubscription, msgIterator);
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

    @Override
    public CompletionStage<Integer> getLag() {
        return topicPartDataManager.getCurrentOffset(partSubscription.getTopicPartition());

    }

    @AllArgsConstructor
    class UnGroupedIterator implements DataIterator<IterableMessage> {
        private final QType qType;
        private final PartSubscription partSubscription;
        private final MsgIterator<Message> iterator;

        @Override
        public IterableMessage peek() {
            return UnGroupedIterableMessage.getInstance(iterator.peek(), partSubscription);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public synchronized IterableMessage next() {
            IterableMessage iterableMessage = UnGroupedIterableMessage.getInstance(iterator.next(), partSubscription);
            incrementCurrSeqNo(qType);
            return iterableMessage;
        }

        @Override
        public void remove() {
            throw new NotImplementedException();
        }

        @Override
        public String name() {
            return iterator.name();
        }
    }

    @AllArgsConstructor
    class UnGroupedFailedMsgIterator implements DataIterator<Message> {
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

        @Override
        public String name() {
            return "Iterator_" + qType + "_" + peek().messageId();
        }
    }
}
