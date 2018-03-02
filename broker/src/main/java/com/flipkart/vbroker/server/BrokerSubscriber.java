package com.flipkart.vbroker.server;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.iterators.SubscriberIterator;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.wrappers.Subscription;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.nonNull;

/**
 * This is the main subscriber which runs on the broker machine and consumes the messages
 * this can be across many topic-partitions spanning across different topics
 */
@Slf4j
public class BrokerSubscriber implements Runnable {

    private final SubscriptionService subscriptionService;
    private final MessageProcessor messageProcessor;
    private final VBrokerConfig config;
    private SubscriberGroupSyncer syncer;
    private volatile AtomicBoolean running = new AtomicBoolean(true);

    public BrokerSubscriber(SubscriptionService subscriptionService,
                            MessageProcessor messageProcessor,
                            VBrokerConfig config) {
        this.subscriptionService = subscriptionService;
        this.messageProcessor = messageProcessor;
        this.config = config;
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
                syncer = new SubscriberGroupSyncer(partSubscribers);
                new Thread(syncer).start();

                log.info("No of partSubscribers are {}", partSubscribers.size());
                log.info("PartSubscribers in the current broker are {}", partSubscribers);
                SubscriberIterator subscriberIterator = new SubscriberIterator(partSubscribers);
                MessageConsumer messageConsumer = MessageConsumer.newInstance(subscriberIterator, messageProcessor);

                long pollTimeMs = config.getSubscriberPollTimeMs();
                while (running.get()) {
                    log.info("Polling for new messages");
                    try {
                        messageConsumer.consume();
                        Thread.sleep(pollTimeMs);
                    } catch (Exception e) {
                        log.error("Exception in consuming the message {}. Sleeping for {} ms", e, pollTimeMs);
                        Thread.sleep(pollTimeMs);
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void stop() {
        if (nonNull(syncer)) {
            syncer.stop();
        }
        this.running.set(false);
    }

    private List<PartSubscriber> getPartSubscribersForCurrentBroker() {
        List<PartSubscriber> partSubscribers = new ArrayList<>();
        Set<Subscription> subscriptions = subscriptionService.getAllSubscriptions().toCompletableFuture().join();
        for (Subscription subscription : subscriptions) {
            List<PartSubscription> partSubscriptions = subscriptionService.getPartSubscriptions(subscription).toCompletableFuture().join();
            for (PartSubscription partSubscription : partSubscriptions) {
                PartSubscriber partSubscriber = subscriptionService.getPartSubscriber(partSubscription).toCompletableFuture().join();
                partSubscribers.add(partSubscriber);
            }
        }
        return partSubscribers;
    }
}
