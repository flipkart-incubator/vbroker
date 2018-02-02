package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hooda on 19/1/18
 */
@EqualsAndHashCode(exclude = {"qType", "currSeqNo"})
//TODO: crude implementation of seqNo. Handle the concurrency here correctly
public class SubscriberGroup implements Iterable<Message> {
    private final MessageGroup messageGroup;
    @Getter
    private final TopicPartition topicPartition;
    @Getter
    @Setter
    private QType qType = QType.MAIN;
    @Getter
    @Setter
    private AtomicInteger currSeqNo = new AtomicInteger(0);
    @Getter
    private volatile AtomicBoolean locked = new AtomicBoolean(false);

    private SubscriberGroup(MessageGroup messageGroup,
                            TopicPartition topicPartition) {
        this.messageGroup = messageGroup;
        this.topicPartition = topicPartition;
    }

    /**
     * @return true if locking for the first time
     * false if already locked
     */
    public boolean lock() {
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
     * @return locked status
     */
    public boolean isLocked() {
        return locked.get();
    }

    private class SubscriberGroupIterator implements PeekingIterator<Message> {
        PeekingIterator<Message> groupIterator = messageGroup.iteratorFrom(currSeqNo.get());

        @Override
        public Message peek() {
            return groupIterator.peek();
        }

        @Override
        public synchronized Message next() {
            Message message = groupIterator.next();
            currSeqNo.incrementAndGet();
            return message;
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

    public static SubscriberGroup newGroup(MessageGroup messageGroup,
                                           TopicPartition topicPartition) {
        return new SubscriberGroup(messageGroup, topicPartition);
    }

    public synchronized List<Message> getUnconsumedMessages(int count) {
        List<Message> messages = messageGroup.getMessages().subList(currSeqNo.get(), currSeqNo.get() + count);
        currSeqNo.set(currSeqNo.get() + count);
        return messages;
    }

    @Override
    public PeekingIterator<Message> iterator() {
        return new SubscriberGroupIterator();
    }

    public String getGroupId() {
        return messageGroup.getGroupId();
    }

    public enum QType {
        MAIN, SIDELINE, RETRY_1, RETRY_2, RETRY_3
    }
}
