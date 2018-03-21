package com.flipkart.vbroker.server;

import com.codahale.metrics.MetricRegistry;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.iterators.SubscriberIterator;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.wrappers.Subscription;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.nonNull;

/**
 * This is the main subscriber which runs on the broker machine and consumes the messages
 * this can be across many topic-partitions spanning across different topics
 */
@Slf4j
public class Subscriber implements Runnable {

    private final SubscriptionService subscriptionService;
    private final MessageProcessor messageProcessor;
    private final VBrokerConfig config;
    private final MetricRegistry metricRegistry;
    private SubscriberGroupSyncer syncer;
    private volatile AtomicBoolean running = new AtomicBoolean(true);

    public Subscriber(SubscriptionService subscriptionService,
                      MessageProcessor messageProcessor,
                      VBrokerConfig config,
                      MetricRegistry metricRegistry) {
        this.subscriptionService = subscriptionService;
        this.messageProcessor = messageProcessor;
        this.config = config;
        this.metricRegistry = metricRegistry;
    }

    public void run() {
        this.running.set(true);
        log.info("Subscriber now running");

        while (running.get()) {
            try {
                List<PartSubscriber> partSubscribers = getPartSubscribersForCurrentBroker();
                syncer = new SubscriberGroupSyncer(partSubscribers);
                new Thread(syncer).start();

                log.info("No of partSubscribers are {}", partSubscribers.size());
                log.info("PartSubscribers in the current broker are {}", partSubscribers);
                SubscriberIterator subscriberIterator = new SubscriberIterator(partSubscribers);
                log.info("Created subscriberIterator");
                MessageConsumer messageConsumer = MessageConsumer.
                    newInstance(subscriberIterator, messageProcessor, metricRegistry);

                long pollTimeMs = config.getSubscriberPollTimeMs();

                //long nonConsumedCountThreshold = (long) Math.pow(10, 9);
                long nonConsumedCountThreshold = 5;
                long notConsumedCount = 0;

                log.info("Subscriber running now and polling for messages...");
                while (running.get()) {
                    log.debug("Polling for new messages");
                    try {
                        boolean consumed = messageConsumer.consume();
                        if (!consumed) {
                            notConsumedCount++;
                            if (notConsumedCount >= nonConsumedCountThreshold) {
                                log.debug("Sleeping for {}ms as in {} tries no messages were consumed",
                                    pollTimeMs, nonConsumedCountThreshold);
                                notConsumedCount = 0;
                                Thread.sleep(pollTimeMs);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Exception in consuming the message {}. Sleeping for {} ms", e, pollTimeMs);
                        Thread.sleep(pollTimeMs);
                    }
                }
            } catch (Exception ex) {
                log.error("Exception in subscriber", ex);
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
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions().toCompletableFuture().join();
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
