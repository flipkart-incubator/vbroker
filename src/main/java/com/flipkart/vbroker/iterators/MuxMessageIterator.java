package com.flipkart.vbroker.iterators;


import com.google.common.collect.PeekingIterator;

import java.util.Queue;

/**
 * Iterator of iterators over SubscriberGroups
 * @param <Message>
 */
public class MuxMessageIterator<Message> implements PeekingIterator<Message> {

    private final Queue<PeekingIterator<Message>> iteratorQueue;
    private PeekingIterator<Message> currIterator;

    public MuxMessageIterator(Queue<PeekingIterator<Message>> iteratorQueue) {
        this.iteratorQueue = iteratorQueue;
        currIterator = iteratorQueue.poll();
    }

    @Override
    public Message peek() {
        return currIterator.peek();
    }

    @Override
    public boolean hasNext() {
        updateIterator();
        return currIterator.hasNext();
    }

    private void updateIterator() {
        if (!currIterator.hasNext()) {
            iteratorQueue.add(currIterator);
            iteratorQueue.remove();
            while (!iteratorQueue.isEmpty()) {
                PeekingIterator<Message> probableIterator = iteratorQueue.poll();
                if (probableIterator.hasNext()) {
                    currIterator = probableIterator;
                } else {
                    iteratorQueue.add(probableIterator);
                }
            }
        }
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