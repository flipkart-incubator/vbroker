package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.VBrokerException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
@EqualsAndHashCode
public class GroupedMessageWithGroup implements IMessageWithGroup {
    @Getter
    private final Message message;
    private final SubscriberGroup subscriberGroup;

    public static GroupedMessageWithGroup newInstance(Message message, SubscriberGroup subscriberGroup) {
        return new GroupedMessageWithGroup(message, subscriberGroup);
    }

    public void setQType(SubscriberGroup.QType qType) {
        this.subscriberGroup.setQType(qType);
    }

    @Override
    public void sideline() {
        log.info("Sidelining the group {}", subscriberGroup.getGroupId());
        this.subscriberGroup.setQType(SubscriberGroup.QType.SIDELINE);
    }

    @Override
    public void retry() {
        SubscriberGroup.QType destinationQType;
        switch (subscriberGroup.getQType()) {
            case MAIN:
                destinationQType = SubscriberGroup.QType.RETRY_1;
                break;
            case RETRY_1:
                destinationQType = SubscriberGroup.QType.RETRY_2;
                break;
            case RETRY_2:
                destinationQType = SubscriberGroup.QType.RETRY_3;
                break;
            case RETRY_3:
                destinationQType = SubscriberGroup.QType.SIDELINE;
                break;
            default:
                throw new VBrokerException("Unknown QType: " + subscriberGroup.getQType());
        }
        this.subscriberGroup.setQType(destinationQType);
    }

    @Override
    public boolean isLocked() {
        return subscriberGroup.isLocked();
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
    public short subscriptionId() {
        return subscriberGroup.getPartSubscription().getSubscriptionId();
    }

    @Override
    public short getTopicId() {
        return subscriberGroup.getTopicPartition().getTopicId();
    }
}
