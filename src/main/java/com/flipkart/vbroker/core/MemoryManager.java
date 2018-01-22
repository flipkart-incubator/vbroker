package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;

public interface MemoryManager {

    public Message allocateMessage(byte[] bytes);

    public void freeMessage(Message message);

    public MessageGroup createNewMessageGroup();

    public void addMessage(Message message, MessageGroup messageGroup);

    //used for upgrade/downgrade
    public void setLevel(MessageGroup messageGroup, MessageGroup.Level level);
}
