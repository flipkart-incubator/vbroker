package com.flipkart.vbroker.server;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.flipkart.vbroker.exceptions.LockFailedException;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.SubscriberIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class MessageConsumer {

    private final SubscriberIterator subscriberIterator;
    private final MessageProcessor messageProcessor;
    //private final MetricRegistry metricRegistry;
    private final Timer msgConsumeTimer;

    private MessageConsumer(SubscriberIterator subscriberIterator,
                            MessageProcessor messageProcessor,
                            MetricRegistry metricRegistry) {
        this.subscriberIterator = subscriberIterator;
        this.messageProcessor = messageProcessor;
        msgConsumeTimer = metricRegistry.timer(MetricRegistry.name(MessageConsumer.class, "total.msg.consuming.time"));
    }

    public static MessageConsumer newInstance(SubscriberIterator subscriberIterator,
                                              MessageProcessor messageProcessor,
                                              MetricRegistry metricRegistry) {
        return new MessageConsumer(subscriberIterator, messageProcessor, metricRegistry);
    }

    /**
     * @return true if consumed successfully, false if unable to acquire lock
     */
    public boolean consume() {
        Timer.Context context = msgConsumeTimer.time();
        if (subscriberIterator.hasNext()) {
            IterableMessage iterableMessage = subscriberIterator.peek();
            Message message = iterableMessage.getMessage();
            //lock the subscriberGroup and process the message
            if (iterableMessage.lock()) {
                log.info("Consuming message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
                messageProcessor.process(iterableMessage)
                    .thenAccept(aVoid -> {
                        iterableMessage.unlock();
                        long totalMsgConsumingTimeNs = context.stop();
                        log.info("Done processing the message {} in {}ms..moving to next message",
                            iterableMessage.getMessage().messageId(), totalMsgConsumingTimeNs / Math.pow(10, 6));
                    });
                return true;
            } else {
                throw new LockFailedException("Failed to acquire an already acquired lock for group: " + message.groupId());
            }
        }
        return false;
    }
}
