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
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hooda on 19/1/18
 */
@Slf4j
@EqualsAndHashCode(exclude = {"qType", "iterator", "topicPartDataManager", "locked"})
//TODO: crude implementation of seqNo. Handle the concurrency here correctly
public class SubscriberGroup {
    private final MessageGroup messageGroup;
    @Getter
    private final TopicPartition topicPartition;
    private final PartSubscription partSubscription;
    private final TopicPartDataManager topicPartDataManager;
    private final AtomicInteger currSeqNo = new AtomicInteger(0);
    private final SubscriberGroupIterator<IterableMessage> iterator;
    private final Object qTypeChangeMonitor = new Object();
    @Getter
    private volatile QType qType = QType.MAIN;
    @Getter
    private volatile AtomicBoolean locked = new AtomicBoolean(false);
    private volatile AtomicBoolean hasLastMsgSucceeded = new AtomicBoolean(true);

    private SubscriberGroup(MessageGroup messageGroup,
                            PartSubscription partSubscription,
                            TopicPartDataManager topicPartDataManager) {
        log.trace("Creating new group {}", messageGroup.getGroupId());
        this.messageGroup = messageGroup;
        this.topicPartition = messageGroup.getTopicPartition();
        this.partSubscription = partSubscription;
        this.topicPartDataManager = topicPartDataManager;
        this.iterator = new SubscriberGroupIteratorImpl(this);
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
    private void forceUnlock() {
        log.debug("Forcefully unlocking the subscriberGroup {} for topic-partition {}", getGroupId(), topicPartition);
        locked.set(false);
    }

    /**
     * @return the offset of the group for the current qType
     */
    public Integer getOffset() {
        return currSeqNo.get();
    }

    public void setOffset(int offset) {
        currSeqNo.set(offset);
    }

    /**
     * method to both unlock the group and advance the iterator for the qType
     * advance only in case the message succeeded
     * if the message failed (ex: in case of http 4xx/5xx) don't increment the iterator as it should be processed again
     * this is required to handle the case where QType is changed while unlocking
     */
    public void advanceIteratorAndUnlockGroup() {
        synchronized (qTypeChangeMonitor) {
            if (hasLastMsgSucceeded.get()) {
                advanceIterator();
            }
            forceUnlock();
        }
    }

    public void setQType(QType qType) {
        synchronized (qTypeChangeMonitor) {
            if (QType.MAIN.equals(qType)) {
                hasLastMsgSucceeded.set(true);
            } else {
                hasLastMsgSucceeded.set(false);
            }
            this.qType = qType;
        }
    }

    /**
     * advance the seqNo to the next message
     * this is currently a stop-gap solution for concurrency cases. Need to better design this
     */
    private void advanceIterator() {
        //TODO: there can be a case where qType gets mutated when iterator next is about to be performed. Validate it
        log.debug("Advancing iterator to next for group {} and QType {}", getGroupId(), qType);
        iterator().next();
    }

    /**
     * @return true if unlocked and false if locked
     */
    public boolean isUnlocked() {
        return !locked.get();
    }

    private int getCurrSeqNo() {
        return currSeqNo.get();
    }

    private synchronized void incrementCurrSeqNo() {
        currSeqNo.incrementAndGet();
    }

    /**
     * @return (Offset of the underlying messageGroup) - (currSeqNo for Main QType)
     */
    public CompletionStage<Integer> getLag() {
        return topicPartDataManager.getCurrentOffset(topicPartition, this.getGroupId())
            .thenApply(offset -> offset - getCurrSeqNo());
    }

    public SubscriberGroupIterator<IterableMessage> iterator() {
        return iterator;
    }

    public SubscriberGroupIterator<IterableMessage> newIterator(int seqNoFrom) {
        log.debug("Creating new iterator for SubscriberGroup {} for qType {} from seqNo {}", getGroupId(), qType, seqNoFrom);
        return new SubscriberGroupIteratorImpl(this, seqNoFrom);
    }

    public String getGroupId() {
        return messageGroup.getGroupId();
    }

    public PartSubscription getPartSubscription() {
        return this.partSubscription;
    }

    @NotThreadSafe
    public class SubscriberGroupIteratorImpl implements SubscriberGroupIterator<IterableMessage> {
        private final SubscriberGroup subscriberGroup;
        private final DataIterator<Message> groupIterator;
        private final boolean autoSeqNoManagement;

        SubscriberGroupIteratorImpl(SubscriberGroup subscriberGroup) {
            log.trace("Creating new subscriberGroupIterator for qType {} and group {}", qType, subscriberGroup.getGroupId());
            this.subscriberGroup = subscriberGroup;
            this.groupIterator = topicPartDataManager.getIterator(topicPartition, getGroupId(), getCurrSeqNo());
            this.autoSeqNoManagement = false;
        }

        SubscriberGroupIteratorImpl(SubscriberGroup subscriberGroup, int seqNoFrom) {
            log.trace("Creating new subscriberGroupIterator for qType {} and group {}", qType, subscriberGroup.getGroupId());
            this.subscriberGroup = subscriberGroup;
            this.groupIterator = topicPartDataManager.getIterator(topicPartition, getGroupId(), seqNoFrom);
            this.autoSeqNoManagement = true;
        }

        @Override
        public boolean isUnlocked() {
            log.debug("SubscriberGroup {} iterator isUnlocked: {}", getGroupId(), subscriberGroup.isUnlocked());
            return subscriberGroup.isUnlocked();
        }

        @Override
        public GroupedIterableMessage peek() {
            Message msg = groupIterator.peek();
            log.debug("Peeking msg {} for group {}", msg.messageId(), msg.groupId());
            return GroupedIterableMessage.newInstance(msg, subscriberGroup);
        }

        @Override
        public GroupedIterableMessage next() {
            log.debug("Moving to next message");
            GroupedIterableMessage messageWithGroup = GroupedIterableMessage.newInstance(groupIterator.next(), subscriberGroup);
            if (autoSeqNoManagement) {
                incrementCurrSeqNo();
                log.debug("Incremented seqNo for group {} to {}", subscriberGroup.getGroupId(), getCurrSeqNo());
            }
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
