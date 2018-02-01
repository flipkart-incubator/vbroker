package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@EqualsAndHashCode(exclude = {"level", "messages"})
public class MessageGroup implements Iterable<Message> {
    private final String groupId;
    private final List<Message> messages;

    public MessageGroup(String groupId, Map map) {
        this.groupId = groupId;
        this.messages = MemoryManager.getVList(groupId);
        ((VList) messages).addObserver((VMap) map);
    }

    public int getUsedCapacity() {
        return ((VList) this.messages).getListUsedCapacity();
    }

    public VList.Level getLevel() {
        return ((VList) this.messages).getLevel();
    }

    public void setLevel(VList.Level level) {
        ((VList) this.messages).setLevel(level);
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
