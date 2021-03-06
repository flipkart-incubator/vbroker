package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.subscribers.GroupedPartSubscriber;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.subscribers.UnGroupedPartSubscriber;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class InMemorySubscriptionService implements SubscriptionService {

    private final TopicService topicService;
    private final TopicPartDataManager topicPartDataManager;
    private final ConcurrentMap<Integer, Subscription> subscriptionsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PartSubscription, PartSubscriber> subscriberMap = new ConcurrentHashMap<>();
    private final SubPartDataManager subPartDataManager;
    private final ExecutorService executorService;

    public InMemorySubscriptionService(TopicService topicService,
                                       TopicPartDataManager topicPartDataManager,
                                       SubPartDataManager subPartDataManager,
                                       ExecutorService executorService) {
        this.topicService = topicService;
        this.topicPartDataManager = topicPartDataManager;
        this.subPartDataManager = subPartDataManager;
        this.executorService = executorService;
    }

    @Override
    public CompletionStage<Subscription> createSubscription(Subscription subscription) {
        return CompletableFuture.supplyAsync(
            () -> subscriptionsMap.putIfAbsent(subscription.id(), subscription), executorService);
    }

    @Override
    public CompletionStage<List<Subscription>> getAllSubscriptions() {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(subscriptionsMap.values()), executorService);
    }

    @Override
    public CompletionStage<PartSubscription> getPartSubscription(Subscription subscription, int partSubscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            if (subscriptionsMap.containsKey(subscription.id())) {
                Subscription existingSub = subscriptionsMap.get(subscription.id());
                //return existingSub.getPartSubscription(partSubscriptionId);
                return SubscriptionUtils.getPartSubscription(existingSub, partSubscriptionId);
            }
            return null;
        }, executorService);
    }

    @Override
    public CompletionStage<PartSubscriber> getPartSubscriber(PartSubscription partSubscription) {
        return CompletableFuture.supplyAsync(() -> {
            log.trace("SubscriberMap contents: {}", subscriberMap);
            log.debug("SubscriberMap status of the part-subscription {} is {}", partSubscription, subscriberMap.containsKey(partSubscription));
            //wanted below to work but its creating a new GroupedPartSubscriber each time though key is already present
            //subscriberMap.putIfAbsent(partSubscription, new GroupedPartSubscriber(partSubscription));

            subscriberMap.computeIfAbsent(partSubscription, partSubscription1 -> {
                PartSubscriber partSubscriber;
                if (partSubscription1.isGrouped()) {
                    partSubscriber = new GroupedPartSubscriber(topicPartDataManager, subPartDataManager, partSubscription1);
                } else {
                    partSubscriber = new UnGroupedPartSubscriber(subPartDataManager, partSubscription1);
                }
                return partSubscriber;
            });
            return subscriberMap.get(partSubscription);
        }, executorService);
    }

    @Override
    public CompletionStage<Subscription> getSubscription(int topicId, int subscriptionId) {
        return CompletableFuture.supplyAsync(() -> subscriptionsMap.get(subscriptionId), executorService);
    }

    @Override
    public CompletionStage<List<Subscription>> getSubscriptionsForTopic(int topicId) {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(subscriptionsMap.values()), executorService);
    }

    @Override
    public CompletionStage<List<Subscription>> getAllSubscriptionsForBroker(String brokerId) {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(subscriptionsMap.values()), executorService);
    }

    @Override
    public CompletionStage<List<PartSubscription>> getPartSubscriptions(Subscription subscription) {
        return topicService.getTopic(subscription.topicId())
            .thenApplyAsync(topic -> SubscriptionUtils.getPartSubscriptions(subscription, topic.partitions()), executorService);
    }

    @Override
    public CompletionStage<Subscription> createSubscriptionAdmin(int id, Subscription subscription) {
        // TODO Auto-generated method stub
        return null;
    }

    public CompletionStage<Integer> getPartSubscriptionLag(PartSubscription partSubscription) {
        return subPartDataManager.getLag(partSubscription);
    }
}
