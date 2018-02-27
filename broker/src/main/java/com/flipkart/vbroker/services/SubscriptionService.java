package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.subscribers.PartSubscriber;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface SubscriptionService {

    public CompletionStage<Subscription> createSubscription(Subscription subscription);

    public CompletionStage<Subscription> getSubscription(short topicId, short subscriptionId);

    public CompletionStage<Set<Subscription>> getAllSubscriptions();

    public CompletionStage<List<Subscription>> getSubscriptionsForTopic(short topicId);

    public CompletionStage<List<Subscription>> getAllSubscriptionsForBroker(String brokerId);

    public CompletionStage<PartSubscription> getPartSubscription(Subscription subscription, short partSubscriptionId);

    public CompletionStage<PartSubscriber> getPartSubscriber(PartSubscription subscription);

    public CompletionStage<List<PartSubscription>> getPartSubscriptions(Subscription subscription);

    public CompletionStage<Integer> getPartSubscriptionLag(PartSubscription partSubscription);
}
