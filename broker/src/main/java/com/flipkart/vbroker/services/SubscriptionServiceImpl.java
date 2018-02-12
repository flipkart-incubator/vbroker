package com.flipkart.vbroker.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.apache.zookeeper.CreateMode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.Subscription;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

	 private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();
    private final VBrokerConfig config;
    private final CuratorService curatorService;
    private final TopicPartDataManager topicPartDataManager;

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
        String path = config.getTopicsPath() + "/" + subscription.getTopic().getId() + "/subscriptions/" + subscription.getId();
        try {
            curatorService.createNodeAndSetData(path, CreateMode.PERSISTENT, subscription.toJson().getBytes());
        } catch (JsonProcessingException e) {
            log.error("Error while parsing json to save subscription.");
            e.printStackTrace();
        }
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

        subscriberMap.computeIfAbsent(partSubscription, partSubscription1 -> {
            return new PartSubscriber(topicPartDataManager, partSubscription1);
        });
        return subscriberMap.get(partSubscription);
    }
    
    @Override
	public Subscription getSubscription(short topicId, short subscriptionId) {
		String subscriptionPath = config.getTopicsPath() + "/" + topicId + "subscriptions" + subscriptionId;
		try {
			curatorService.getData(subscriptionPath).handle((data, exception) -> {
				try {
					return MAPPER.readValue(data, Subscription.class);
				} catch (IOException e) {
					log.error("Error while parsing subscription data");
					e.printStackTrace();
					return null;
				}
			}).toCompletableFuture().get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Interrupted exception while fetching subscription data");
			e.printStackTrace();
		}
		return null;
	}




	@Override
	public List<Subscription> getAllSubscriptionsForBroker(String brokerId) {
		String hostPath = "/brokers/" + brokerId + "/subscriptions";
		List<Subscription> subscriptions = new ArrayList<>();
		List<String> subscriptionIds;
		try {
			subscriptionIds = curatorService.getChildren(hostPath).handle((data, exception) -> {
				return data;
			}).toCompletableFuture().get();
			for (String id : subscriptionIds) {
				subscriptions
						.add(this.getSubscription(Short.valueOf(id.split("-")[0]), Short.valueOf(id.split("-")[1])));
			}
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while fetching all subscriptions");
			e.printStackTrace();
		}
		return subscriptions;
	}

	@Override
	public List<Subscription> getSubscriptionsForTopic(short topicId) {
		String path = config.getTopicsPath() + "/" + topicId;
		List<Subscription> subscriptions = new ArrayList<>();
		try {
			List<String> subscriptionIds = curatorService.getChildren(path).handle((data, exception) -> {
				return data;
			}).toCompletableFuture().get();
			for (String id : subscriptionIds) {
				subscriptions.add(this.getSubscription(topicId, Short.valueOf(id)));
			}
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while fetching all subscriptions");
			e.printStackTrace();
		}
		return subscriptions;
	}


}
