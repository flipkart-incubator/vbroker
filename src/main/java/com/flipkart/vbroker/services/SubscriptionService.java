package com.flipkart.vbroker.services;

import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.subscribers.PartSubscription;
import com.flipkart.vbroker.core.Subscription;

import java.util.Set;

public interface SubscriptionService {

    public void createPartSubscription(Subscription subscription, PartSubscription partSubscription);

    public void createSubscription(Subscription subscription);

    public Subscription getSubscription(short subscriptionId);

    public Set<Subscription> getAllSubscriptions();

    public PartSubscription getPartSubscription(Subscription subscription, short partSubscriptionId);

    public PartSubscriber getPartSubscriber(PartSubscription subscription);
}
