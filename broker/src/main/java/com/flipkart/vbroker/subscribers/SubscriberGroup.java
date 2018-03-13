package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.DataIterator;
import com.flipkart.vbroker.iterators.SubscriberGroupIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.NotThreadSafe;
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
public class SubscriberGroup {
    private final MessageGroup messageGroup;
    @Getter
    private final TopicPartition topicPartition;
    private final PartSubscription partSubscription;
    private final TopicPartDataManager topicPartDataManager;
    private final Map<QType, AtomicInteger> currSeqNoMap = new ConcurrentHashMap<>();
    private final Map<QType, SubscriberGroupIterator<IterableMessage>> iteratorMap = new ConcurrentHashMap<>();
    @Getter
    private volatile QType qType = QType.MAIN;
    @Getter
    private volatile AtomicBoolean locked = new AtomicBoolean(false);
    private final Object qTypeChangeMonitor = new Object();

    private SubscriberGroup(MessageGroup messageGroup,
                            PartSubscription partSubscription,
                            TopicPartDataManager topicPartDataManager) {
        log.trace("Creating new group {}", messageGroup.getGroupId());
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
        log.info("Locking the subscriberGroup {} for topic-partition {}", getGroupId(), topicPartition);
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
    private void forceUnlock() {
        log.info("Forcefully unlocking the subscriberGroup {} for topic-partition {}", getGroupId(), topicPartition);
        locked.set(false);
    }

    /**
     * method to both unlock the group and advance the iterator for the qType
     * this is required to handle the case where QType is changed while unlocking
     */
    public void forceUnlockAndAdvanceIterator() {
        synchronized (qTypeChangeMonitor) {
            forceUnlock();
            advanceIterator();
        }
    }

    public void setQType(QType qType) {
        synchronized (qTypeChangeMonitor) {
            this.qType = qType;
        }
    }

    /**
     * advance the seqNo to the next message
     * this is currently a stop-gap solution for concurrency cases. Need to better design this
     */
    private void advanceIterator() {
        //TODO: there can be a case where qType gets mutated when iterator next is about to be performed. Validate it
        log.info("Advancing iterator to next for QType {}", qType);
        iterator(qType).next();
    }

    /**
     * @return true if unlocked and false if locked
     */
    public boolean isUnlocked() {
        return !locked.get();
    }

    private int getCurrSeqNo(QType qType) {
        currSeqNoMap.putIfAbsent(qType, new AtomicInteger(0));
        //currSeqNoMap.computeIfAbsent(qType, qType1 -> new AtomicInteger(0));
        return currSeqNoMap.get(qType).get();
    }

    private synchronized void incrementCurrSeqNo(QType qType) {
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

    public SubscriberGroupIterator<IterableMessage> iterator(QType qType) {
        if (!iteratorMap.containsKey(qType)) {
            log.trace("Creating a new SubGroupIterator for qType {} and group {}", qType, getGroupId());
            iteratorMap.putIfAbsent(qType, new SubscriberGroupIteratorImpl(qType, this));
        }
        return iteratorMap.get(qType);
    }

    public String getGroupId() {
        return messageGroup.getGroupId();
    }

    public PartSubscription getPartSubscription() {
        return this.partSubscription;
    }

    @NotThreadSafe
    public class SubscriberGroupIteratorImpl implements SubscriberGroupIterator<IterableMessage> {
        private final QType qType;
        private final SubscriberGroup subscriberGroup;
        private final DataIterator<Message> groupIterator;

        SubscriberGroupIteratorImpl(QType qType, SubscriberGroup subscriberGroup) {
            log.trace("Creating new subscriberGroupIterator for qType {} and group {}", qType, subscriberGroup.getGroupId());
            this.qType = qType;
            this.subscriberGroup = subscriberGroup;
            this.groupIterator = topicPartDataManager.getIterator(topicPartition, getGroupId(), getCurrSeqNo(qType));
        }

        @Override
        public boolean isUnlocked() {
            log.debug("SubscriberGroup {} iterator isUnlocked: {}", getGroupId(), subscriberGroup.isUnlocked());
            return subscriberGroup.isUnlocked();
        }

        @Override
        public GroupedIterableMessage peek() {
            Message msg = groupIterator.peek();
            log.debug("Peeking msg {}", msg.messageId());
            return GroupedIterableMessage.newInstance(msg, subscriberGroup);
        }

        @Override
        public GroupedIterableMessage next() {
            log.debug("Moving to next message");
            GroupedIterableMessage messageWithGroup = GroupedIterableMessage.newInstance(groupIterator.next(), subscriberGroup);
            incrementCurrSeqNo(qType);
            log.info("Incremented seqNo for group {} to {}", subscriberGroup.getGroupId(), getCurrSeqNo(qType));
            return messageWithGroup;
        }

        @Override
        public void remove() {
            throw new VBrokerException("Unsupported operation");
        }

        @Override
        public boolean hasNext() {
            return groupIterator.hasNext();
        }

        @Override
        public String name() {
            return subscriberGroup.getGroupId();
        }
    }
}
