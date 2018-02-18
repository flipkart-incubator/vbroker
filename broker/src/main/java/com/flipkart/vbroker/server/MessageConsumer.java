package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.LockFailedException;
import com.flipkart.vbroker.iterators.SubscriberIterator;
import com.flipkart.vbroker.subscribers.GroupedMessageWithGroup;
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
        GroupedMessageWithGroup messageWithGroup = subscriberIterator.peek();
        Message message = messageWithGroup.getMessage();

        //lock the subscriberGroup and process the message
        if (messageWithGroup.lock()) {
            log.debug("Consuming message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
            messageProcessor.process(messageWithGroup);

            log.trace("Done processing..moving to next message");
            //move over to the next message
            subscriberIterator.next();
            log.trace("Moved to next message");
        } else {
            throw new LockFailedException("Failed to acquire an already acquired lock for group: " + message.groupId());
        }
    }
}
