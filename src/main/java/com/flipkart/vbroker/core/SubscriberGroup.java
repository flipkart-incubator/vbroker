package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hooda on 19/1/18
 */
@EqualsAndHashCode(exclude = {"qType", "currSeqNo"})
public class SubscriberGroup implements Iterable<Message> {
    public enum QType {
        MAIN, SIDELINE, RETRY_1, RETRY_2, RETRY_3
    }

    private final MessageGroup messageGroup;
    @Getter
    private final TopicPartition topicPartition;
    @Getter
    @Setter
    private QType qType = QType.MAIN;
    @Getter
    @Setter
    private AtomicInteger currSeqNo = new AtomicInteger(0);

    public SubscriberGroup(MessageGroup messageGroup,
                           TopicPartition topicPartition) {
        this.messageGroup = messageGroup;
        this.topicPartition = topicPartition;
    }

    public static SubscriberGroup newGroup(MessageGroup messageGroup,
                                           TopicPartition topicPartition) {
        return new SubscriberGroup(messageGroup, topicPartition);
    }

    public List<Message> getUnconsumedMessages(int count) {
        return messageGroup.getMessages().subList(currSeqNo.get(), currSeqNo.get() + count);
    }

    @Override
    public PeekingIterator<Message> iterator() {
        return new PeekingIterator<Message>() {
            PeekingIterator<Message> groupIterator = messageGroup.iteratorFrom(currSeqNo.get());

            @Override
            public Message peek() {
                return groupIterator.peek();
            }

            @Override
            public Message next() {
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
        };
    }

    public String getGroupId() {
        return messageGroup.getGroupId();
    }
}
