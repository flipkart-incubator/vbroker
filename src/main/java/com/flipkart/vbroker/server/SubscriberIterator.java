package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.PartSubscriber;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@Slf4j
public class SubscriberIterator implements PeekingIterator<Message> {

    private final Queue<PeekingIterator<Message>> iteratorQueue = new ArrayDeque<>();
    private PeekingIterator<Message> currIterator;

    public SubscriberIterator(List<PartSubscriber> partSubscribers) {
        for (PartSubscriber partSubscriber : partSubscribers) {
            PeekingIterator<Message> iterator = partSubscriber.iterator();
            iteratorQueue.add(iterator);
        }

        if (iteratorQueue.size() == 0) {
            throw new VBrokerException("Cannot create a iterator as queue is empty");
        }
        currIterator = iteratorQueue.poll();
    }

    @Override
    public Message peek() {
        return currIterator.peek();
    }

    @Override
    public boolean hasNext() {
        if (currIterator.hasNext()) {
            return true;
        }

        for (int i = 0; i < iteratorQueue.size(); i++) {
            PeekingIterator<Message> iterator = iteratorQueue.peek();
            if (iterator.hasNext()) {
                iteratorQueue.add(currIterator);
                currIterator = iterator;
                break;
            }
            iteratorQueue.add(iteratorQueue.poll());
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
