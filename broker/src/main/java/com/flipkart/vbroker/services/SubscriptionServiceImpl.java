package com.flipkart.vbroker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.subscribers.IPartSubscriber;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@AllArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();
    private final VBrokerConfig config;
    private final CuratorService curatorService;
    private final TopicPartDataManager topicPartDataManager;
    private final SubPartDataManager subPartDataManager;
    private final TopicService topicService;

    private final ConcurrentMap<Short, Subscription> subscriptionsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PartSubscription, IPartSubscriber> subscriberMap = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<Subscription> createSubscription(Subscription subscription) {
        subscriptionsMap.putIfAbsent(subscription.id(), subscription);
        String path = config.getTopicsPath() + "/" + subscription.topicId() + "/subscriptions/" + subscription.id();

        return curatorService.createNodeAndSetData(path, CreateMode.PERSISTENT, subscription.getByteBuffer().array())
            .event().thenApplyAsync(watchedEvent -> subscription);
    }

    @Override
    public CompletionStage<Set<Subscription>> getAllSubscriptions() {
        return CompletableFuture.supplyAsync(() -> new HashSet<>(subscriptionsMap.values()));
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
                return new PartSubscriber(topicPartDataManager, subPartDataManager, partSubscription1);
            });
            return subscriberMap.get(partSubscription);
        });
    }

    @Override
    public CompletionStage<Subscription> getSubscription(short topicId, short subscriptionId) {
        String subscriptionPath = config.getTopicsPath() + "/" + topicId + "/subscriptions/" + subscriptionId;
        return curatorService.getData(subscriptionPath).handle((data, exception) -> {
            try {
                return MAPPER.readValue(data, Subscription.class);
            } catch (IOException e) {
                log.error("Error while parsing subscription data");
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public CompletionStage<List<Subscription>> getAllSubscriptionsForBroker(String brokerId) {
        //TODO: fix this method
        String hostPath = "/brokers/" + brokerId + "/subscriptions";
        List<Subscription> subscriptions = new ArrayList<>();
        CompletionStage<List<String>> handleStage = curatorService.getChildren(hostPath).handle((data, exception) -> data);
        List<String> subscriptionIds = handleStage.toCompletableFuture().join();
        for (String id : subscriptionIds) {
            CompletionStage<Subscription> subscriptionStage = getSubscription(Short.valueOf(id.split("-")[0]), Short.valueOf(id.split("-")[1]));
            subscriptions.add(subscriptionStage.toCompletableFuture().join());
        }
        return CompletableFuture.supplyAsync(() -> subscriptions);
    }

    @Override
    public CompletionStage<List<Subscription>> getSubscriptionsForTopic(short topicId) {
        String path = config.getTopicsPath() + "/" + topicId + "/subscriptions";
        List<Subscription> subscriptions = new ArrayList<>();
        List<String> subscriptionIds = curatorService.getChildren(path).handle((data, exception) -> data).toCompletableFuture().join();
        if (subscriptionIds != null) {
            for (String id : subscriptionIds) {
                CompletionStage<Subscription> subscriptionStage = getSubscription(topicId, Short.valueOf(id));
                subscriptions.add(subscriptionStage.toCompletableFuture().join());
            }
        }
        return CompletableFuture.supplyAsync(() -> subscriptions);
    }

    @Override
    public CompletionStage<List<PartSubscription>> getPartSubscriptions(Subscription subscription) {
        //temporary until this entire method is made async. never to this!
        Topic topic = topicService.getTopic(subscription.topicId()).toCompletableFuture().join();
        return CompletableFuture.supplyAsync(() -> SubscriptionUtils.getPartSubscriptions(subscription, topic.partitions()));
    }
}
