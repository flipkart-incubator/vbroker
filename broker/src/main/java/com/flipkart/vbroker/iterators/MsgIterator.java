package com.flipkart.vbroker.iterators;

import com.google.common.collect.PeekingIterator;

/**
 * a peeking iterator with a name
 *
 * @param <E>
 */
public interface MsgIterator<E> extends PeekingIterator<E> {

    /**
     * @return the name
     */
    String name();

    /**
     * @return if we are free to iterate over it
     */
    default boolean isUnlocked() {
        return true;
    }
}
