package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.PartSubscriber;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.Subscription;
import com.flipkart.vbroker.iterators.SubscriberIterator;
import com.flipkart.vbroker.services.SubscriberMetadataService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicMetadataService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the main subscriber which runs on the broker machine and consumes the messages
 * this can be across many topic-partitions spanning across different topics
 */
@Slf4j
public class BrokerSubscriber implements Runnable {

    private final SubscriptionService subscriptionService;
    private final MessageProcessor messageProcessor;
    private final SubscriberMetadataService subscriberMetadataService;
    private final TopicMetadataService topicMetadataService;
    private SubscriberGroupSyncer syncer;
    private volatile AtomicBoolean running = new AtomicBoolean(true);

    public BrokerSubscriber(SubscriptionService subscriptionService, MessageProcessor messageProcessor, SubscriberMetadataService subscriberMetadataService, TopicMetadataService topicMetadataService) {
        this.subscriptionService = subscriptionService;
        this.messageProcessor = messageProcessor;
        this.subscriberMetadataService = subscriberMetadataService;
        this.topicMetadataService = topicMetadataService;
    }

    public void run() {
        this.running.set(true);
        log.info("BrokerSubscriber now running");

        while (running.get()) {
            try {
                long timeMs = 1000;
                log.info("Sleeping for {} milli secs before connecting to server", timeMs);
                Thread.sleep(timeMs);

                List<PartSubscriber> partSubscribers = getPartSubscribersForCurrentBroker();
                syncer = new SubscriberGroupSyncer(partSubscribers, subscriberMetadataService, topicMetadataService);
                new Thread(syncer).start();

                log.info("No of partSubscribers are {}", partSubscribers.size());
                SubscriberIterator subscriberIterator = new SubscriberIterator(partSubscribers);
                MessageConsumer messageConsumer = MessageConsumer.newInstance(subscriberIterator, messageProcessor);

                long pollTimeMs = 2 * 1000;
                while (running.get()) {
                    log.info("Polling for new messages");
                    while (running.get() && subscriberIterator.hasNext()) {
                        log.trace("Consuming..");
                        try {
                            messageConsumer.consume();
                        } catch (Exception e) {
                            log.error("Exception in consuming the message", e);
                            Thread.sleep(pollTimeMs);
                        }
                    }
                    Thread.sleep(pollTimeMs);
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void stop() {
        if (Objects.nonNull(syncer)) {
            syncer.stop();
        }
        this.running.set(false);
    }

    private List<PartSubscriber> getPartSubscribersForCurrentBroker() {
        List<PartSubscriber> partSubscribers = new ArrayList<>();
        Set<Subscription> allSubscriptions = subscriptionService.getAllSubscriptions();
        for (Subscription subscription : allSubscriptions) {
            List<PartSubscription> partSubscriptions = subscription.getPartSubscriptions();
            for (PartSubscription partSubscription : partSubscriptions) {
                PartSubscriber partSubscriber = subscriptionService.getPartSubscriber(partSubscription);
                partSubscribers.add(partSubscriber);
            }
        }
        return partSubscribers;
    }
}
