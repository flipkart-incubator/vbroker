package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.MessageWithGroup;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.LockFailedException;
import com.flipkart.vbroker.iterators.SubscriberIterator;
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

    public void consume() throws Exception {
        //peek the message first
        MessageWithGroup messageWithGroup = subscriberIterator.peek();
        Message message = messageWithGroup.getMessage();

        //lock the subscriberGroup and process the message
        if (messageWithGroup.lockGroup()) {
            log.info("Consuming message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
            messageProcessor.process(messageWithGroup);
            //move over to the next message
            subscriberIterator.next();
        } else {
            throw new LockFailedException("Failed to acquire an already acquired lock for group: " + message.groupId());
        }
    }
}
