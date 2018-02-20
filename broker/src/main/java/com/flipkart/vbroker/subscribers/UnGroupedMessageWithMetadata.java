package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class UnGroupedMessageWithMetadata implements MessageWithMetadata {
    @Getter
    private final Message message;
    private final PartSubscription partSubscription;

    @Override
    public boolean isUnlocked() {
        return true;
    }

    @Override
    public boolean lock() {
        return true;
    }

    @Override
    public void unlock() {
    }

    @Override
    public String getGroupId() {
        return message.groupId();
    }

    @Override
    public PartSubscription getPartSubscription() {
        return partSubscription;
    }

    @Override
    public short subscriptionId() {
        return partSubscription.getSubscriptionId();
    }

    @Override
    public short getTopicId() {
        return partSubscription.getTopicPartition().getTopicId();
    }

    @Override
    public QType getQType() {
        return null;
    }
}
