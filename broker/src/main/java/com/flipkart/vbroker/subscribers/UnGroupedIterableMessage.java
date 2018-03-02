package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.flatbuf.Message;
import lombok.Getter;

public class UnGroupedIterableMessage implements IterableMessage {
    @Getter
    private final Message message;
    private final PartSubscription partSubscription;
    private QType qType;

    public UnGroupedIterableMessage(Message message, PartSubscription partSubscription) {
        this.message = message;
        this.partSubscription = partSubscription;
        this.qType = QType.MAIN;
    }

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
    public int subscriptionId() {
        return partSubscription.getSubscriptionId();
    }

    @Override
    public int getTopicId() {
        return partSubscription.getTopicPartition().getTopicId();
    }

    @Override
    public QType getQType() {
        return qType;
    }

    @Override
    public void setQType(QType qType) {
        this.qType = qType;
    }
}
