package com.flipkart.vbroker.iterators;

import com.flipkart.vbroker.subscribers.IterableMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static java.util.Objects.nonNull;

@Slf4j
public abstract class PartSubscriberIterator implements MsgIterator<IterableMessage> {
    private MsgIterator<IterableMessage> currIterator;

    protected abstract Optional<MsgIterator<IterableMessage>> nextIterator();

    @Override
    public String name() {
        return currIterator.name();
    }

    @Override
    public IterableMessage peek() {
        IterableMessage msg = currIterator.peek();
        log.info("Peeking msg {}", msg.getMessage().messageId());
        return msg;
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
            //if (isIteratorActive()) return true;
            Optional<MsgIterator<IterableMessage>> iteratorOpt = nextIterator();
            if (iteratorOpt.isPresent()) {
                currIterator = iteratorOpt.get();
                return currIterator.hasNext();
            }
        } catch (Exception e) {
            log.error("Exception in nextIterator/hasNext", e);
        }
        return false;
    }

    private boolean isIteratorActive() {
        return nonNull(currIterator)
            && currIterator.hasNext()
            && currIterator.peek().isUnlocked();
    }
}
