package com.flipkart.vbroker.iterators;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.AllArgsConstructor;

import java.util.Iterator;

public class VBIterators {

    public static <T> VIterator<T> peekingIterator(Iterator<? extends T> iterator, String name) {
        return new VBPeekingIteratorImpl<>(Iterators.peekingIterator(iterator), name);
    }

    @AllArgsConstructor
    private static class VBPeekingIteratorImpl<T> implements VIterator<T> {
        private final PeekingIterator<T> peekingIterator;
        private final String name;

        @Override
        public T peek() {
            return peekingIterator.peek();
        }

        @Override
        public boolean hasNext() {
            return peekingIterator.hasNext();
        }

        @Override
        public T next() {
            return peekingIterator.next();
        }

        @Override
        public void remove() {
            peekingIterator.remove();
        }

        @Override
        public String name() {
            return name;
        }
    }
}
