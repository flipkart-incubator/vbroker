package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscriber;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.Subscription;
import com.flipkart.vbroker.exceptions.VBrokerException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Slf4j
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
        log.trace("SubscriberMap contents: {}", subscriberMap);
        log.debug("SubscriberMap status of the part-subscription {} is {}", partSubscription, subscriberMap.containsKey(partSubscription));
        //wanted below to work but its creating a new PartSubscriber each time though key is already present
        //subscriberMap.putIfAbsent(partSubscription, new PartSubscriber(partSubscription));

        subscriberMap.computeIfAbsent(partSubscription, new Function<PartSubscription, PartSubscriber>() {
            @Override
            public PartSubscriber apply(PartSubscription partSubscription) {
                return new PartSubscriber(partSubscription, 1);
            }
        });
        PartSubscriber partSub = subscriberMap.get(partSubscription);
        partSub.refreshSubscriberGroups(); //can compute twice if its just created - should be okay as new subscriber will be empty in no of groups

        return partSub;
    }
}
