package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.core.MessageWithGroup;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@Slf4j
public class SubscriberIterator implements PeekingIterator<MessageWithGroup> {

    private final Queue<PeekingIterator<MessageWithGroup>> iteratorQueue = new ArrayDeque<>();
    private PeekingIterator<MessageWithGroup> currIterator;

    public SubscriberIterator(List<PartSubscriber> partSubscribers) {
        for (PartSubscriber partSubscriber : partSubscribers) {
            PeekingIterator<MessageWithGroup> iterator = partSubscriber.iterator();
            iteratorQueue.add(iterator);
        }

        if (iteratorQueue.size() == 0) {
            throw new VBrokerException("Cannot create a iterator as queue is empty");
        }
        currIterator = iteratorQueue.poll();
    }

    @Override
    public MessageWithGroup peek() {
        return currIterator.peek();
    }

    @Override
    public boolean hasNext() {
        if (currIterator.hasNext() && !currIterator.peek().isGroupLocked()) {
            return true;
        }

        log.trace("IteratorQ size: {}", iteratorQueue.size());
        for (int i = 0; i < iteratorQueue.size(); i++) {
            PeekingIterator<MessageWithGroup> iterator = iteratorQueue.peek();
            if (iterator.hasNext() && !iterator.peek().isGroupLocked()) {
                iteratorQueue.add(currIterator);
                currIterator = iterator;
                break;
            }
            iteratorQueue.add(iteratorQueue.poll());
        }

        return currIterator.hasNext();
    }


    @Override

    public MessageWithGroup next() {
        return currIterator.next();
    }

    @Override
    public void remove() {
        currIterator.remove();
    }

}
