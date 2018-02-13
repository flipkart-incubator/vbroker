package com.flipkart.vbroker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.utils.JsonUtils;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

@Slf4j
@AllArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();
    private final VBrokerConfig config;
    private final CuratorService curatorService;
    private final TopicPartDataManager topicPartDataManager;
    private final TopicService topicService;

    private final ConcurrentMap<Short, Subscription> subscriptionsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PartSubscription, PartSubscriber> subscriberMap = new ConcurrentHashMap<>();


    @Override
    public void createSubscription(Subscription subscription) {
        subscriptionsMap.putIfAbsent(subscription.subscriptionId(), subscription);
        String path = config.getTopicsPath() + "/" + subscription.topicId() + "/subscriptions/" + subscription.subscriptionId();
//        try {
        curatorService.createNodeAndSetData(path, CreateMode.PERSISTENT, subscription.getByteBuffer().array());
//        } catch (JsonProcessingException e) {
//            log.error("Error while parsing json to save subscription.");
//            e.printStackTrace();
//        }
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
        String subscriptionPath = config.getTopicsPath() + "/" + topicId + "/subscriptions/" + subscriptionId;
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
        String path = config.getTopicsPath() + "/" + topicId + "/subscriptions";
        List<Subscription> subscriptions = new ArrayList<>();
        try {
            List<String> subscriptionIds = curatorService.getChildren(path).handle((data, exception) -> {
                return data;
            }).toCompletableFuture().get();
            if (subscriptionIds != null) {
                for (String id : subscriptionIds) {
                    subscriptions.add(this.getSubscription(topicId, Short.valueOf(id)));
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while fetching all subscriptions");
            e.printStackTrace();
        }
        return subscriptions;
    }

    @Override
    public List<PartSubscription> getPartSubscriptions(Subscription subscription) {
        //temporary until this entire method is made async. never to this!
        Topic topic = topicService.getTopic(subscription.topicId()).toCompletableFuture().join();
        return SubscriptionUtils.getPartSubscriptions(subscription, topic.partitions());
    }
}
