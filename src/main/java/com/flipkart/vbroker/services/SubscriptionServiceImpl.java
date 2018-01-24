package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscriber;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.Subscription;
import com.flipkart.vbroker.exceptions.VBrokerException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SubscriptionServiceImpl implements SubscriptionService {

    private final ConcurrentMap<Short, Subscription> subscriptionsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PartSubscription, PartSubscriber> subscriberMap = new ConcurrentHashMap<>();

    @Override
    public void createPartSubscription(Subscription subscription, PartSubscription partSubscription) {
        if (subscriptionsMap.containsKey(subscription.getId())) {
            subscriptionsMap.get(subscription.getId()).addPartSubscription(partSubscription);
        } else {
            throw new VBrokerException("Subscription " + subscription + " not present to add partSubscription: " + subscription);
        }
    }

    @Override
    public void createSubscription(Subscription subscription) {
        subscriptionsMap.putIfAbsent(subscription.getId(), subscription);
    }

    @Override
    public Subscription getSubscription(short subscriptionId) {
        return subscriptionsMap.get(subscriptionId);
    }

    @Override
    public Set<Subscription> getAllSubscriptions() {
        return new HashSet<>(subscriptionsMap.values());
    }

    @Override
    public PartSubscription getPartSubscription(Subscription subscription, short partSubscriptionId) {
        if (subscriptionsMap.containsKey(subscription.getId())) {
            Subscription existingSub = subscriptionsMap.get(subscription.getId());
            return existingSub.getPartSubscription(partSubscriptionId);
        }
        return null;
    }

    @Override
    public PartSubscriber getPartSubscriber(PartSubscription partSubscription) {
        subscriberMap.putIfAbsent(partSubscription, new PartSubscriber(partSubscription));
        return subscriberMap.get(partSubscription);
    }
}
