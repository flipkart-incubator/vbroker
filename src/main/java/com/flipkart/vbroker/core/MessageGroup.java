package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@EqualsAndHashCode(exclude = {"memoryManager", "messages"})
public class MessageGroup implements Iterable<Message> {
    private final String groupId;
    private final List<Message> messages;
    private final MemoryManager memoryManager = new LocalMemoryManager();

    public MessageGroup(short id, short partitionId, String groupId) {
        this.groupId = groupId;
        this.messages = memoryManager.getMessageList(groupId, id, partitionId);
    }

    public MessageList.Level getLevel() {
        return ((MessageList) this.messages).getLevel();
    }

    public void setLevel(MessageList.Level level) {
        ((MessageList) this.messages).setLevel(level);
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
