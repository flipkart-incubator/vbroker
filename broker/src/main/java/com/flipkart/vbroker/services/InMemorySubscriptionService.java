package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.subscribers.IPartSubscriber;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.subscribers.UnGroupedPartSubscriber;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class InMemorySubscriptionService implements SubscriptionService {

    private final TopicService topicService;
    private final TopicPartDataManager topicPartDataManager;
    private final ConcurrentMap<Short, Subscription> subscriptionsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PartSubscription, IPartSubscriber> subscriberMap = new ConcurrentHashMap<>();
    private final SubPartDataManager subPartDataManager;

    public InMemorySubscriptionService(TopicService topicService,
                                       TopicPartDataManager topicPartDataManager,
                                       SubPartDataManager subPartDataManager) {
        this.topicService = topicService;
        this.topicPartDataManager = topicPartDataManager;
        this.subPartDataManager = subPartDataManager;
    }

    @Override
    public CompletionStage<Subscription> createSubscription(Subscription subscription) {
        return CompletableFuture.supplyAsync(() -> subscriptionsMap.putIfAbsent(subscription.id(), subscription));
    }

    @Override
    public CompletionStage<List<Subscription>> getAllSubscriptions() {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(subscriptionsMap.values()));
    }

    @Override
    public CompletionStage<PartSubscription> getPartSubscription(Subscription subscription, short partSubscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            if (subscriptionsMap.containsKey(subscription.id())) {
                Subscription existingSub = subscriptionsMap.get(subscription.id());
                //return existingSub.getPartSubscription(partSubscriptionId);
                return SubscriptionUtils.getPartSubscription(existingSub, partSubscriptionId);
            }
            return null;
        });
    }

    @Override
    public CompletionStage<IPartSubscriber> getPartSubscriber(PartSubscription partSubscription) {
        return CompletableFuture.supplyAsync(() -> {
            log.trace("SubscriberMap contents: {}", subscriberMap);
            log.debug("SubscriberMap status of the part-subscription {} is {}", partSubscription, subscriberMap.containsKey(partSubscription));
            //wanted below to work but its creating a new PartSubscriber each time though key is already present
            //subscriberMap.putIfAbsent(partSubscription, new PartSubscriber(partSubscription));

            subscriberMap.computeIfAbsent(partSubscription, partSubscription1 -> {
                IPartSubscriber partSubscriber;
                if (partSubscription1.isGrouped()) {
                    partSubscriber = new PartSubscriber(topicPartDataManager, subPartDataManager, partSubscription1);
                } else {
                    partSubscriber = new UnGroupedPartSubscriber(subPartDataManager, partSubscription1);
                }
                return partSubscriber;
            });
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

    @Override
    public CompletionStage<Subscription> createSubscriptionAdmin(short id, Subscription subscription) {
        // TODO Auto-generated method stub
        return null;
    }
}
