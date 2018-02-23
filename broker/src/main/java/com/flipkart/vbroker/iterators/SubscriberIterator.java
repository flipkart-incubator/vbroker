package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.subscribers.IPartSubscriber;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@Slf4j
public class SubscriberIterator implements PeekingIterator<IterableMessage> {

    private final Queue<PeekingIterator<IterableMessage>> iteratorQueue = new ArrayDeque<>();
    private PeekingIterator<IterableMessage> currIterator;

    public SubscriberIterator(List<IPartSubscriber> partSubscribers) {
        for (IPartSubscriber partSubscriber : partSubscribers) {
            PeekingIterator<IterableMessage> iterator = partSubscriber.iterator();
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
            PeekingIterator<IterableMessage> iterator = iteratorQueue.peek();
            if (iterator.hasNext() && iterator.peek().isUnlocked()) {
                iteratorQueue.add(currIterator);
                currIterator = iterator;
                break;
            }
            iteratorQueue.add(iteratorQueue.poll());
        }
        return currIterator.hasNext();
    }

    public PeekingIterator<IterableMessage> getCurrIterator() {
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

}
