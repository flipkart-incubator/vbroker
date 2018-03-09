package com.flipkart.vbroker.iterators;

import com.google.common.collect.PeekingIterator;

/**
 * a peeking iterator with a name
 *
 * @param <E>
 */
public interface VIterator<E> extends PeekingIterator<E> {

    /**
     * @return the name
     */
    String name();
}
