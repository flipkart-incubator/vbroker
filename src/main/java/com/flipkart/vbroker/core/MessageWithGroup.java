package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MessageWithGroup {
    private final Message message;
    private final SubscriberGroup subscriberGroup;

    public static MessageWithGroup newInstance(Message message, SubscriberGroup subscriberGroup) {
        return new MessageWithGroup(message, subscriberGroup);
    }
}
