package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.subscribers.QType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Iterator hierarchy
 * <p>
 * Subscriber => []PartSubscribers
 * a Subscriber is a running thread which consumes messages for given part subscribers
 * SubscriberIterator is an iterator for the Subscriber
 * <p>
 * Ideally and (this is what we do), we should have exactly one SubscriberIterator per broker
 * <p>
 * SubscriberIterator -> PartSubscriberIterator -> DataIterator(TopicPartData/SubPartData)
 * explanation:
 * <p>
 * for *grouped*:
 * SubscriberIterator -> []PartSubscriberIterator
 * PartSubscriberIterator -> []SubscriberGroupIterator
 * SubscriberGroupIterator -> DataIterator
 * <p>
 * for *un-grouped*:
 * SubscriberIterator -> []PartSubscriberIterator
 * PartSubscriberIterator -> DataIterator
 * <p>
 * <p>
 * SubscriberIterator -> an iterator over multiple PartSubscriberIterators (of diff PartSubscriptions)
 * <p>
 * if *grouped* PartSubscription,
 * PartSubscriberIterator -> []SubscriberGroupIterator
 * PartSubscriberIterator -> an iterator over multiple SubscriberGroups
 * <p>
 * if *un-grouped* PartSubscription,
 * PartSubscriberIterator -> an iterator over direct data queue (MainQ/SQ/UQ)
 */
@Slf4j
public class SubscriberIterator implements VIterator<IterableMessage> {

    private final Queue<VIterator<IterableMessage>> iteratorQueue = new ArrayDeque<>();
    private VIterator<IterableMessage> currIterator;

    public SubscriberIterator(List<PartSubscriber> partSubscribers) {
        for (PartSubscriber partSubscriber : partSubscribers) {
            VIterator<IterableMessage> iterator = partSubscriber.iterator(QType.MAIN);
            iteratorQueue.add(iterator);
        }

        if (iteratorQueue.size() == 0) {
            throw new VBrokerException("Cannot create a iterator as queue is empty");
        }
        currIterator = iteratorQueue.poll();
    }

    @Override
    public IterableMessage peek() {
        return currIterator.peek();
    }

    @Override
    public boolean hasNext() {
        log.trace("CurrIterator {} hasNext {}", currIterator, currIterator.hasNext());
        if (currIterator.hasNext() && currIterator.peek().isUnlocked()) {
            return true;
        }

        for (int i = 0; i < iteratorQueue.size(); i++) {
            VIterator<IterableMessage> iterator = iteratorQueue.peek();
            if (iterator.hasNext() && iterator.peek().isUnlocked()) {
                iteratorQueue.add(currIterator);
                currIterator = iterator;
                break;
            }
            iteratorQueue.add(iteratorQueue.poll());
        }
        return currIterator.hasNext();
    }

    public VIterator<IterableMessage> getCurrIterator() {
        return currIterator;
    }

    @Override
    public IterableMessage next() {
        return currIterator.next();
    }

    @Override
    public void remove() {
        currIterator.remove();
    }

    @Override
    public String name() {
        return currIterator.name();
    }
}
