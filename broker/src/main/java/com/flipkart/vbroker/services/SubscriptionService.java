package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.Subscription;
import com.flipkart.vbroker.subscribers.PartSubscriber;

import java.util.List;
import java.util.Set;

public interface SubscriptionService {

    public void createPartSubscription(Subscription subscription, PartSubscription partSubscription);

    public void createSubscription(Subscription subscription);

    public Subscription getSubscription(short topicId, short subscriptionId);

    public Set<Subscription> getAllSubscriptions();
    
    public List<Subscription> getSubscriptionsForTopic(short topicId);
    
    public List<Subscription> getAllSubscriptionsForBroker(String brokerId);

    public PartSubscription getPartSubscription(Subscription subscription, short partSubscriptionId);

    public PartSubscriber getPartSubscriber(PartSubscription subscription);
}
