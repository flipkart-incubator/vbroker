package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.PartSubscriber;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.Subscription;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.services.SubscriptionService;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class BrokerSubscriber implements Runnable {

    private final SubscriptionService subscriptionService;
    private volatile AtomicBoolean running = new AtomicBoolean(true);

    public BrokerSubscriber(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
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
                SubscriberGroupSyncer syncer = new SubscriberGroupSyncer(partSubscribers);
                new Thread(syncer).start();

                log.info("No of partSubscribers are {}", partSubscribers.size());
                PeekingIterator<Message> subscriberIterator = new SubscriberIterator(partSubscribers);

                long pollTimeMs = 2 * 1000;
                while (running.get()) {
                    log.debug("Polling for new messages");
                    while (subscriberIterator.hasNext()) {
                        Message message = subscriberIterator.peek();
                        process(message);
                        subscriberIterator.next();
                    }

                    Thread.sleep(pollTimeMs);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void process(Message message) {
        log.info("Processing message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
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
