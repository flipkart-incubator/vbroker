package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hooda on 19/1/18
 */
@Slf4j
@EqualsAndHashCode(exclude = {"qType", "currSeqNo", "topicPartDataManager"})
//TODO: crude implementation of seqNo. Handle the concurrency here correctly
public class SubscriberGroup implements Iterable<MessageWithGroup> {
    private final MessageGroup messageGroup;
    @Getter
    private final TopicPartition topicPartition;
    private final PartSubscription partSubscription;
    private final TopicPartDataManager topicPartDataManager;
    @Getter
    @Setter
    private QType qType = QType.MAIN;
    @Getter
    @Setter
    private AtomicInteger currSeqNo = new AtomicInteger(0);
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

    @Override
    public PeekingIterator<MessageWithGroup> iterator() {
        return new SubscriberGroupIterator(this);
    }

    public String getGroupId() {
        return messageGroup.getGroupId();
    }

    public PartSubscription getPartSubscription() {
        return this.partSubscription;
    }

    public enum QType {
        MAIN, SIDELINE, RETRY_1, RETRY_2, RETRY_3
    }

    private class SubscriberGroupIterator implements PeekingIterator<MessageWithGroup> {

        SubscriberGroup subscriberGroup;
        PeekingIterator<Message> groupIterator = topicPartDataManager.getIterator(topicPartition, getGroupId(), currSeqNo.get());

        public SubscriberGroupIterator(SubscriberGroup subscriberGroup) {
            this.subscriberGroup = subscriberGroup;
        }

        @Override
        public MessageWithGroup peek() {
            return MessageWithGroup.newInstance(groupIterator.peek(), subscriberGroup);
        }

        @Override
        public synchronized MessageWithGroup next() {
            MessageWithGroup messageWithGroup = MessageWithGroup.newInstance(groupIterator.next(), subscriberGroup);
            currSeqNo.incrementAndGet();
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
    }
}
