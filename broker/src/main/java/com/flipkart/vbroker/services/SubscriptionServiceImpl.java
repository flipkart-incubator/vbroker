package com.flipkart.vbroker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.subscribers.GroupedPartSubscriber;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.utils.JsonUtils;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import com.flipkart.vbroker.wrappers.Topic;
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

    private final ConcurrentMap<Integer, Subscription> subscriptionsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PartSubscription, PartSubscriber> subscriberMap = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<Subscription> createSubscription(Subscription subscription) {
        subscriptionsMap.putIfAbsent(subscription.id(), subscription);
        String path = config.getTopicsPath() + "/" + subscription.topicId() + "/subscriptions/" + subscription.id();

        return curatorService.createNodeAndSetData(path, CreateMode.PERSISTENT, subscription.toBytes())
            .event().thenApplyAsync(watchedEvent -> subscription);
    }

    @Override
    public CompletionStage<Set<Subscription>> getAllSubscriptions() {
        return CompletableFuture.supplyAsync(() -> new HashSet<>(subscriptionsMap.values()));
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
        });
    }

    @Override
    public CompletionStage<PartSubscriber> getPartSubscriber(PartSubscription partSubscription) {
        return CompletableFuture.supplyAsync(() -> {
            log.trace("SubscriberMap contents: {}", subscriberMap);
            log.debug("SubscriberMap status of the part-subscription {} is {}", partSubscription, subscriberMap.containsKey(partSubscription));
            //wanted below to work but its creating a new GroupedPartSubscriber each time though key is already present
            //subscriberMap.putIfAbsent(partSubscription, new GroupedPartSubscriber(partSubscription));

            subscriberMap.computeIfAbsent(partSubscription, partSubscription1 -> {
                return new GroupedPartSubscriber(topicPartDataManager, subPartDataManager, partSubscription1);
            });
            return subscriberMap.get(partSubscription);
        });
    }

    @Override
    public CompletionStage<Subscription> getSubscription(int topicId, int subscriptionId) {
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
    public CompletionStage<List<Subscription>> getSubscriptionsForTopic(int topicId) {
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

    @Override
    public CompletionStage<Integer> getPartSubscriptionLag(PartSubscription partSubscription) {
        return subPartDataManager.getLag(partSubscription);
    }
}
