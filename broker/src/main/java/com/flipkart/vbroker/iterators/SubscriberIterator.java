package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.subscribers.IMessageWithGroup;
import com.flipkart.vbroker.subscribers.IPartSubscriber;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@Slf4j
public class SubscriberIterator implements PeekingIterator<IMessageWithGroup> {

    private final Queue<PeekingIterator<IMessageWithGroup>> iteratorQueue = new ArrayDeque<>();
    private PeekingIterator<IMessageWithGroup> currIterator;

    public SubscriberIterator(List<IPartSubscriber> partSubscribers) {
        for (IPartSubscriber partSubscriber : partSubscribers) {
            PeekingIterator<IMessageWithGroup> iterator = partSubscriber.iterator();
            iteratorQueue.add(iterator);
        }

        if (iteratorQueue.size() == 0) {
            throw new VBrokerException("Cannot create a iterator as queue is empty");
        }
        currIterator = iteratorQueue.poll();
    }

    @Override
    public IMessageWithGroup peek() {
        return currIterator.peek();
    }

    @Override
    public boolean hasNext() {
        if (currIterator.hasNext() && !currIterator.peek().isLocked()) {
            return true;
        }

        for (int i = 0; i < iteratorQueue.size(); i++) {
            PeekingIterator<IMessageWithGroup> iterator = iteratorQueue.peek();
            if (iterator.hasNext() && !iterator.peek().isLocked()) {
                iteratorQueue.add(currIterator);
                currIterator = iterator;
                break;
            }
            iteratorQueue.add(iteratorQueue.poll());
        }
        return currIterator.hasNext();
    }

    @Override
    public IMessageWithGroup next() {
        return currIterator.next();
    }

    @Override
    public void remove() {
        currIterator.remove();
    }

}
