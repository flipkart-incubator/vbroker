package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class MessageWithGroup {
    @Getter
    private final Message message;
    private final SubscriberGroup subscriberGroup;

    public static MessageWithGroup newInstance(Message message, SubscriberGroup subscriberGroup) {
        return new MessageWithGroup(message, subscriberGroup);
    }

    public boolean isGroupLocked() {
        return subscriberGroup.isLocked();
    }

    public boolean lockGroup() {
        return subscriberGroup.lock();
    }

    public void forceUnlockGroup() {
        subscriberGroup.forceUnlock();
    }
}
