package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class UngroupedMessageWithGroup implements IMessageWithGroup {
    @Getter
    private final Message message;

    @Override
    public void sideline() {

    }

    @Override
    public void retry() {

    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean lock() {
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public short subscriptionId() {
        return 0;
    }

    @Override
    public short getTopicId() {
        return 0;
    }
}
