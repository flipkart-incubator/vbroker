package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.PartSubscriber;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class SubscriberIterator implements PeekingIterator<Message> {

    private final Queue<PeekingIterator<Message>> iteratorQueue = new ArrayDeque<>();
    private PeekingIterator<Message> currIterator;

    public SubscriberIterator(List<PartSubscriber> partSubscribers) {
        for (PartSubscriber partSubscriber : partSubscribers) {
            PeekingIterator<Message> iterator = partSubscriber.iterator();
            iteratorQueue.add(iterator);
        }

        if (partSubscribers.size() > 0) {
            currIterator = iteratorQueue.poll();
        } else {
            throw new VBrokerException("Cannot create a iterator as queue is empty");
        }
    }

    @Override
    public Message peek() {
        return currIterator.peek();
    }

    @Override
    public boolean hasNext() {
        if (!currIterator.hasNext()) {
            iteratorQueue.add(currIterator);
            for (int i = 0; i < iteratorQueue.size(); i++) {
                currIterator = iteratorQueue.poll();
                if (currIterator.hasNext()) {
                    break;
                }
                iteratorQueue.add(currIterator);
            }
        }
        return currIterator.hasNext();
    }

    @Override
    public Message next() {
        return currIterator.next();
    }

    @Override
    public void remove() {
        currIterator.remove();
    }

}
