package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.LockFailedException;
import com.flipkart.vbroker.iterators.SubscriberIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.google.common.collect.PeekingIterator;
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
        if (subscriberIterator.hasNext()) {
            PeekingIterator<IterableMessage> currIterator = subscriberIterator.getCurrIterator();
            //peek the message first
            IterableMessage iterableMessage = currIterator.peek();
            Message message = iterableMessage.getMessage();

            //lock the subscriberGroup and process the message
            if (iterableMessage.lock()) {
                log.debug("Consuming message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
                messageProcessor.process(iterableMessage)
                    .thenAccept(aVoid -> {
                        log.info("Done processing the message {} ..moving to next message",
                            iterableMessage.getMessage().messageId());
                        currIterator.next();
                        iterableMessage.unlock();
                    });
            } else {
                throw new LockFailedException("Failed to acquire an already acquired lock for group: " + message.groupId());
            }
        }
    }
}
