package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.subscribers.IterableMessage;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static java.util.Objects.nonNull;

@Slf4j
public abstract class PartSubscriberIterator implements PeekingIterator<IterableMessage> {
    private PeekingIterator<IterableMessage> currIterator;

    protected abstract Optional<PeekingIterator<IterableMessage>> nextIterator();

    @Override
    public IterableMessage peek() {
        return currIterator.peek();
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
    public boolean hasNext() {
        try {
            if (isCurrIteratorActive()) return true;

            Optional<PeekingIterator<IterableMessage>> iteratorOpt = nextIterator();
            if (iteratorOpt.isPresent()) {
                currIterator = iteratorOpt.get();
                return currIterator.hasNext();
            }
        } catch (Exception e) {
            log.error("Exception in nextIterator/hasNext", e);
        }
        return false;
    }

    private boolean isCurrIteratorActive() {
        return nonNull(currIterator)
            && currIterator.hasNext()
            && currIterator.peek().isUnlocked();
    }
}
