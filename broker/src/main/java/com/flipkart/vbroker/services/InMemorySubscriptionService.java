package com.flipkart.vbroker.services;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.utils.SubscriptionUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

//    @Override
//    public void createPartSubscription(Subscription subscription, PartSubscription partSubscription) {
//        if (subscriptionsMap.containsKey(subscription.subscriptionId())) {
//            subscriptionsMap.get(subscription.subscriptionId()).addPartSubscription(partSubscription);
//        } else {
//            throw new VBrokerException("Subscription " + subscription + " not present to add partSubscription: " + subscription);
//        }
//    }

    @Override
    public void createSubscription(Subscription subscription) {
        subscriptionsMap.putIfAbsent(subscription.subscriptionId(), subscription);
    }

    @Override
    public Set<Subscription> getAllSubscriptions() {
        return new HashSet<>(subscriptionsMap.values());
    }

    @Override
    public PartSubscription getPartSubscription(Subscription subscription, short partSubscriptionId) {
        if (subscriptionsMap.containsKey(subscription.subscriptionId())) {
            Subscription existingSub = subscriptionsMap.get(subscription.subscriptionId());
            //return existingSub.getPartSubscription(partSubscriptionId);
            return SubscriptionUtils.getPartSubscription(existingSub, partSubscriptionId);
        }
        return null;
    }

    @Override
    public PartSubscriber getPartSubscriber(PartSubscription partSubscription) {
        log.trace("SubscriberMap contents: {}", subscriberMap);
        log.debug("SubscriberMap status of the part-subscription {} is {}", partSubscription, subscriberMap.containsKey(partSubscription));
        //wanted below to work but its creating a new PartSubscriber each time though key is already present
        //subscriberMap.putIfAbsent(partSubscription, new PartSubscriber(partSubscription));

        subscriberMap.computeIfAbsent(partSubscription, partSubscription1 -> {
            return new PartSubscriber(topicPartDataManager, partSubscription1);
        });
        return subscriberMap.get(partSubscription);
    }

	@Override
	public Subscription getSubscription(short topicId, short subscriptionId) {
		return subscriptionsMap.get(subscriptionId);
	}

	@Override
	public List<Subscription> getSubscriptionsForTopic(short topicId) {
		 return new ArrayList<>(subscriptionsMap.values());
	}

	@Override
	public List<Subscription> getAllSubscriptionsForBroker(String brokerId) {
		 return new ArrayList<>(subscriptionsMap.values());
	}

	@Override
	public List<PartSubscription> getPartSubscriptions(Subscription subscription) {
		Topic topic = topicService.getTopic(subscription.topicId());
        return SubscriptionUtils.getPartSubscriptions(subscription, topic.partitions());
	}
}
