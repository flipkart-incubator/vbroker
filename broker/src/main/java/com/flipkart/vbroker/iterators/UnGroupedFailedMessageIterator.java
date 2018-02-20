package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.flipkart.vbroker.subscribers.MessageWithMetadata;
import com.google.common.collect.PeekingIterator;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class UnGroupedFailedMessageIterator implements PeekingIterator<Message> {

    AtomicInteger index = new AtomicInteger();

    @Override
    public Message peek() {
        return getMessageWithMetadata(index.get()).getMessage();
    }

    protected abstract int getTotalSize();

    protected abstract MessageWithMetadata getMessageWithMetadata(int indexNo);

    @Override
    public Message next() {
        return getMessageWithMetadata(index.getAndIncrement()).getMessage();
    }

    @Override
    public void remove() {
        throw new NotImplementedException("Remove not supported for iterator");
    }

    @Override
    public boolean hasNext() {
        return index.get() < getTotalSize();
    }
}
