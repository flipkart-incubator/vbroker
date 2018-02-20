package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.subscribers.MessageWithMetadata;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static java.util.Objects.nonNull;

@Slf4j
public abstract class PartSubscriberIterator implements PeekingIterator<MessageWithMetadata> {
    private PeekingIterator<MessageWithMetadata> currIterator;

    protected abstract Optional<PeekingIterator<MessageWithMetadata>> nextIterator();

    @Override
    public MessageWithMetadata peek() {
        return currIterator.peek();
    }

    @Override
    public MessageWithMetadata next() {
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

            Optional<PeekingIterator<MessageWithMetadata>> iteratorOpt = nextIterator();
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
