package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class InMemorySubscriptionService implements SubscriptionService {

    private final TopicService topicService;
    private final TopicPartDataManager topicPartDataManager;
    private final ConcurrentMap<Short, Subscription> subscriptionsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PartSubscription, PartSubscriber> subscriberMap = new ConcurrentHashMap<>();

    public InMemorySubscriptionService(TopicService topicService, TopicPartDataManager topicPartDataManager) {
        this.topicService = topicService;
        this.topicPartDataManager = topicPartDataManager;
    }

    @Override
    public CompletionStage<Subscription> createSubscription(Subscription subscription) {
        return CompletableFuture.supplyAsync(() -> subscriptionsMap.putIfAbsent(subscription.subscriptionId(), subscription));
    }

    @Override
    public CompletionStage<Set<Subscription>> getAllSubscriptions() {
        return CompletableFuture.supplyAsync(() -> new HashSet<>(subscriptionsMap.values()));
    }

    @Override
    public CompletionStage<PartSubscription> getPartSubscription(Subscription subscription, short partSubscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            if (subscriptionsMap.containsKey(subscription.subscriptionId())) {
                Subscription existingSub = subscriptionsMap.get(subscription.subscriptionId());
                //return existingSub.getPartSubscription(partSubscriptionId);
                return SubscriptionUtils.getPartSubscription(existingSub, partSubscriptionId);
            }
            return null;
        });
    }

    @Override
    public CompletionStage<PartSubscriber> getPartSubscriber(PartSubscription partSubscription) {
        return CompletableFuture.supplyAsync(() -> {
            log.trace("SubscriberMap contents: {}", subscriberMap);
            log.debug("SubscriberMap status of the part-subscription {} is {}", partSubscription, subscriberMap.containsKey(partSubscription));
            //wanted below to work but its creating a new PartSubscriber each time though key is already present
            //subscriberMap.putIfAbsent(partSubscription, new PartSubscriber(partSubscription));

            subscriberMap.computeIfAbsent(partSubscription, partSubscription1 -> new PartSubscriber(topicPartDataManager, partSubscription1));
            return subscriberMap.get(partSubscription);
        });
    }

    @Override
    public CompletionStage<Subscription> getSubscription(short topicId, short subscriptionId) {
        return CompletableFuture.supplyAsync(() -> subscriptionsMap.get(subscriptionId));
    }

    @Override
    public CompletionStage<List<Subscription>> getSubscriptionsForTopic(short topicId) {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(subscriptionsMap.values()));
    }

    @Override
    public CompletionStage<List<Subscription>> getAllSubscriptionsForBroker(String brokerId) {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(subscriptionsMap.values()));
    }

    @Override
    public CompletionStage<List<PartSubscription>> getPartSubscriptions(Subscription subscription) {
        return topicService.getTopic(subscription.topicId())
            .thenApplyAsync(topic -> SubscriptionUtils.getPartSubscriptions(subscription, topic.partitions()));
    }
}
