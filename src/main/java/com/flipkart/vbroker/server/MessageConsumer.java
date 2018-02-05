package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.MessageWithGroup;
import com.flipkart.vbroker.core.SubscriberGroup;
import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class MessageConsumer {

    private final SubscriberIterator subscriberIterator;
    private final MessageProcessor messageProcessor;

    public static MessageConsumer newInstance(SubscriberIterator subscriberIterator,
                                              MessageProcessor messageProcessor) {
        return new MessageConsumer(subscriberIterator, messageProcessor);
    }

    public void consume() {
        //peek the message first
        MessageWithGroup messageWithGroup = subscriberIterator.peek();
        Message message = messageWithGroup.getMessage();
        SubscriberGroup subscriberGroup = messageWithGroup.getSubscriberGroup();

        log.info("Consuming message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());

        //lock the subscriberGroup and process the message
        if (subscriberGroup.lock()) {
            messageProcessor.process(message);
            //move over to the next message
            subscriberIterator.next();
        }
    }
}
