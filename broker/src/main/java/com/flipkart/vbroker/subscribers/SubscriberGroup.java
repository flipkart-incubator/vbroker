package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.VIterator;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hooda on 19/1/18
 */
@Slf4j
@EqualsAndHashCode(exclude = {"qType", "currSeqNoMap", "topicPartDataManager", "locked"})
//TODO: crude implementation of seqNo. Handle the concurrency here correctly
public class SubscriberGroup implements Iterable<IterableMessage> {
    private final MessageGroup messageGroup;
    @Getter
    private final TopicPartition topicPartition;
    private final PartSubscription partSubscription;
    private final TopicPartDataManager topicPartDataManager;
    private final Map<QType, AtomicInteger> currSeqNoMap = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private QType qType = QType.MAIN;
    @Getter
    private volatile AtomicBoolean locked = new AtomicBoolean(false);

    private SubscriberGroup(MessageGroup messageGroup,
                            PartSubscription partSubscription,
                            TopicPartDataManager topicPartDataManager) {
        this.messageGroup = messageGroup;
        this.topicPartition = messageGroup.getTopicPartition();
        this.partSubscription = partSubscription;
        this.topicPartDataManager = topicPartDataManager;
    }

    public static SubscriberGroup newGroup(MessageGroup messageGroup,
                                           PartSubscription partSubscription,
                                           TopicPartDataManager topicPartDataManager) {
        return new SubscriberGroup(messageGroup, partSubscription, topicPartDataManager);
    }

    /**
     * @return true if locking for the first time
     * false if already locked
     */
    public boolean lock() {
        log.debug("Locking the subscriberGroup {} for topic-partition {}", getGroupId(), topicPartition);
        return locked.compareAndSet(false, true);
    }

    /**
     * @return true if successfully unlocked
     * false if already unlocked
     */
    public boolean unlock() {
        return locked.compareAndSet(true, false);
    }

    /**
     * forcefully set the state as unlocked
     */
    public void forceUnlock() {
        log.debug("Forcefully unlocking the subscriberGroup {} for topic-partition {}", getGroupId(), topicPartition);
        locked.set(false);
    }

    /**
     * @return locked status
     */
    public boolean isLocked() {
        return locked.get();
    }

    private int getCurrSeqNo(QType qType) {
        currSeqNoMap.putIfAbsent(qType, new AtomicInteger(0));
        //currSeqNoMap.computeIfAbsent(qType, qType1 -> new AtomicInteger(0));
        return currSeqNoMap.get(qType).get();
    }

    private void incrementCurrSeqNo(QType qType) {
        currSeqNoMap.putIfAbsent(qType, new AtomicInteger(0));
        //currSeqNoMap.computeIfAbsent(qType, qType1 -> new AtomicInteger(0));
        currSeqNoMap.get(qType).incrementAndGet();
    }

    /**
     * @return (Offset of the underlying messageGroup) - (currSeqNo for Main QType)
     */
    public CompletionStage<Integer> getLag() {
        return topicPartDataManager.getCurrentOffset(topicPartition, this.getGroupId())
            .thenApply(offset -> offset - getCurrSeqNo(QType.MAIN));
    }

    @Override
    public VIterator<IterableMessage> iterator() {
        return new SubscriberGroupIterator(QType.MAIN, this);
    }

    public VIterator<IterableMessage> sidelineIterator() {
        return new SubscriberGroupIterator(QType.SIDELINE, this);
    }

    public VIterator<IterableMessage> retryIterator(int retryNo) {
        QType qType = QType.retryQType(retryNo);
        return new SubscriberGroupIterator(qType, this);
    }

    public VIterator<IterableMessage> iterator(QType qType) {
        return new SubscriberGroupIterator(qType, this);
    }

    public String getGroupId() {
        return messageGroup.getGroupId();
    }

    public PartSubscription getPartSubscription() {
        return this.partSubscription;
    }

    private class SubscriberGroupIterator implements VIterator<IterableMessage> {
        private QType qType;
        private SubscriberGroup subscriberGroup;
        private PeekingIterator<Message> groupIterator;

        public SubscriberGroupIterator(QType qType, SubscriberGroup subscriberGroup) {
            this.qType = qType;
            this.subscriberGroup = subscriberGroup;
            this.groupIterator = topicPartDataManager.getIterator(topicPartition, getGroupId(), getCurrSeqNo(qType));
        }

        @Override
        public synchronized GroupedIterableMessage peek() {
            return GroupedIterableMessage.newInstance(groupIterator.peek(), subscriberGroup);
        }

        @Override
        public synchronized GroupedIterableMessage next() {
            GroupedIterableMessage messageWithGroup = GroupedIterableMessage.newInstance(groupIterator.next(), subscriberGroup);
            incrementCurrSeqNo(qType);
            return messageWithGroup;
        }

        @Override
        public void remove() {
            throw new VBrokerException("Unsupported operation");
        }

        @Override
        public synchronized boolean hasNext() {
            return groupIterator.hasNext();
        }

        @Override
        public String name() {
            return subscriberGroup.getGroupId();
        }
    }
}
