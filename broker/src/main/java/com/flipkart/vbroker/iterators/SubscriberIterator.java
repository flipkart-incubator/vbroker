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
public class SubscriberIterator implements MsgIterator<IterableMessage> {

    private final Queue<PartSubscriberIterator<IterableMessage>> iteratorQueue = new ArrayDeque<>();
    private PartSubscriberIterator<IterableMessage> currIterator;

    public SubscriberIterator(List<PartSubscriber> partSubscribers) {
        for (PartSubscriber partSubscriber : partSubscribers) {
            PartSubscriberIterator<IterableMessage> iterator = partSubscriber.iterator(QType.MAIN);
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
        if (isIteratorHavingNext(currIterator)) {
            return true;
        }

        for (int i = 0; i < iteratorQueue.size(); i++) {
            PartSubscriberIterator<IterableMessage> iterator = iteratorQueue.peek();
            if (isIteratorHavingNext(iterator)) {
                iteratorQueue.add(currIterator);
                currIterator = iterator;
                break;
            }
            iteratorQueue.add(iteratorQueue.poll());
        }
        return isIteratorHavingNext(currIterator);
    }

    private boolean isIteratorHavingNext(PartSubscriberIterator<IterableMessage> iterator) {
        //TODO: validate the commenting of checking isUnlocked()
        //the logic being that locking is checked in the sub iterators of this
        //here check for isUnlocked is required as we don't have to re-peek the message under execution
        return iterator.hasNext() && iterator.isUnlocked();
    }

    public PartSubscriberIterator<IterableMessage> getCurrIterator() {
        return currIterator;
    }

    @Override
    public IterableMessage next() {
        log.debug("Moving to next message");
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
