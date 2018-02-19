package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class UnGroupedMessageWithGroup implements IMessageWithGroup {
    @Getter
    private final Message message;
    private final PartSubscription partSubscription;

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
        return true;
    }

    @Override
    public void unlock() {
    }

    @Override
    public short subscriptionId() {
        return partSubscription.getSubscriptionId();
    }

    @Override
    public short getTopicId() {
        return partSubscription.getTopicPartition().getTopicId();
    }
}
