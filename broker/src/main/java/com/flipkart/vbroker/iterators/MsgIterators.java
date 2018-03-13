package com.flipkart.vbroker.iterators;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.AllArgsConstructor;

import java.util.Iterator;

public class MsgIterators {

    public static <T> MsgIterator<T> msgIterator(Iterator<? extends T> iterator, String name) {
        return new VBPeekingIteratorImpl<>(Iterators.peekingIterator(iterator), name);
    }

    public static <T> DataIterator<T> dataIterator(MsgIterator<T> iterator) {
        return new DataIteratorImpl<>(iterator);
    }

    public static <T> PartSubscriberIterator<T> partSubscriberIterator(MsgIterator<T> iterator) {
        return new PartSubscriberIteratorImpl<>(iterator);
    }

    @AllArgsConstructor
    private static class PartSubscriberIteratorImpl<T> implements PartSubscriberIterator<T> {
        private final MsgIterator<T> iterator;

        @Override
        public synchronized boolean isUnlocked() {
            return iterator.isUnlocked();
        }

        @Override
        public synchronized String name() {
            return iterator.name();
        }

        @Override
        public synchronized T peek() {
            return iterator.peek();
        }

        @Override
        public synchronized T next() {
            return iterator.next();
        }

        @Override
        public synchronized void remove() {
            iterator.remove();
        }

        @Override
        public synchronized boolean hasNext() {
            return iterator.hasNext();
        }
    }

    @AllArgsConstructor
    private static class DataIteratorImpl<T> implements DataIterator<T> {
        private final MsgIterator<T> iterator;

        @Override
        public boolean isUnlocked() {
            return iterator.isUnlocked();
        }

        @Override
        public String name() {
            return iterator.name();
        }

        @Override
        public T peek() {
            return iterator.peek();
        }

        @Override
        public T next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
    }

    @AllArgsConstructor
    private static class VBPeekingIteratorImpl<T> implements MsgIterator<T> {
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
