package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.subscribers.IPartSubscriber;
import com.flipkart.vbroker.subscribers.MessageWithMetadata;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@Slf4j
public class SubscriberIterator implements PeekingIterator<MessageWithMetadata> {

    private final Queue<PeekingIterator<MessageWithMetadata>> iteratorQueue = new ArrayDeque<>();
    private PeekingIterator<MessageWithMetadata> currIterator;

    public SubscriberIterator(List<IPartSubscriber> partSubscribers) {
        for (IPartSubscriber partSubscriber : partSubscribers) {
            PeekingIterator<MessageWithMetadata> iterator = partSubscriber.iterator();
            iteratorQueue.add(iterator);
        }

        if (iteratorQueue.size() == 0) {
            throw new VBrokerException("Cannot create a iterator as queue is empty");
        }
        currIterator = iteratorQueue.poll();
    }

    @Override
    public MessageWithMetadata peek() {
        return currIterator.peek();
    }

    @Override
    public boolean hasNext() {
        log.trace("CurrIterator {} hasNext {}", currIterator, currIterator.hasNext());
        if (currIterator.hasNext() && currIterator.peek().isUnlocked()) {
            return true;
        }

        for (int i = 0; i < iteratorQueue.size(); i++) {
            PeekingIterator<MessageWithMetadata> iterator = iteratorQueue.peek();
            if (iterator.hasNext() && iterator.peek().isUnlocked()) {
                iteratorQueue.add(currIterator);
                currIterator = iterator;
                break;
            }
            iteratorQueue.add(iteratorQueue.poll());
        }
        return currIterator.hasNext();
    }

    @Override
    public MessageWithMetadata next() {
        return currIterator.next();
    }

    @Override
    public void remove() {
        currIterator.remove();
    }

}
