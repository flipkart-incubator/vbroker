package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@EqualsAndHashCode(exclude = {"level", "messages"})
public class MessageGroup implements Iterable<Message> {
    public enum Level {
        L1, L2, L3
    }

    private final String groupId;
    private Level level;
    private final List<Message> messages = new LinkedList<>();

    public MessageGroup(String groupId) {
        this.groupId = groupId;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void appendMessage(Message message) {
        this.messages.add(message);
    }

    public PeekingIterator<Message> iteratorFrom(int offset) {
        return Iterators.peekingIterator(messages.listIterator(offset));
    }

    @Override
    public PeekingIterator<Message> iterator() {
        return Iterators.peekingIterator(messages.iterator());
    }
}
