package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
@EqualsAndHashCode
public class GroupedMessageWithMetadata implements MessageWithMetadata {
    @Getter
    private final Message message;
    private final SubscriberGroup subscriberGroup;

    public static GroupedMessageWithMetadata newInstance(Message message, SubscriberGroup subscriberGroup) {
        return new GroupedMessageWithMetadata(message, subscriberGroup);
    }

    @Override
    public boolean isUnlocked() {
        return !subscriberGroup.isLocked();
    }

    @Override
    public boolean lock() {
        log.info("Locking the group {}", subscriberGroup.getGroupId());
        return subscriberGroup.lock();
    }

    @Override
    public void unlock() {
        log.info("Unlocking the group {}", subscriberGroup.getGroupId());
        subscriberGroup.forceUnlock();
    }

    @Override
    public String getGroupId() {
        return subscriberGroup.getGroupId();
    }

    @Override
    public PartSubscription getPartSubscription() {
        return subscriberGroup.getPartSubscription();
    }

    @Override
    public short subscriptionId() {
        return subscriberGroup.getPartSubscription().getSubscriptionId();
    }

    @Override
    public short getTopicId() {
        return subscriberGroup.getTopicPartition().getTopicId();
    }

    @Override
    public QType getQType() {
        return subscriberGroup.getQType();
    }

    public void setQType(QType qType) {
        this.subscriberGroup.setQType(qType);
    }
}
