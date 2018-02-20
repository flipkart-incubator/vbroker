package com.flipkart.vbroker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.CallbackConfig;
import com.flipkart.vbroker.entities.CodeRange;
import com.flipkart.vbroker.entities.FilterKeyValues;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.exceptions.SubscriptionCreationException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.subscribers.IPartSubscriber;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.utils.JsonUtils;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import com.google.flatbuffers.FlatBufferBuilder;
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
    private final TopicService topicService;

    private final ConcurrentMap<Short, Subscription> subscriptionsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PartSubscription, IPartSubscriber> subscriberMap = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<Subscription> createSubscription(Subscription subscription) {
        String path = config.getTopicsPath() + "/" + subscription.topicId() + "/subscriptions/" + "0";

        return curatorService.createNodeAndSetData(path, CreateMode.PERSISTENT, subscription.getByteBuffer().array())
            .handle((data, exception) -> {
                if (exception != null) {
                    log.error("Exception in curator node create and set data stage {} ", exception);
                    throw new SubscriptionCreationException(exception.getMessage());
                } else {
                    String arr[] = data.split("/");
                    if (!data.contains("/") || arr.length != 2) {
                        log.error("Invalid id {}", data);
                        throw new SubscriptionCreationException("Invalid id data from curator" + data);
                    }
                    String subscriptionId = arr[1];
                    log.info("Created subscription with id - " + subscriptionId);
                    return newSubscriptionFromSubscription(Short.valueOf(subscriptionId), subscription);
                }
            });
    }

    private Subscription newSubscriptionFromSubscription(short id, Subscription subscription) {
        FlatBufferBuilder dataBuilder = new FlatBufferBuilder();
        int codeRangesLength = subscription.callbackConfig().codeRangesLength();
        int[] codeRanges = new int[codeRangesLength];
        for (int i = 0; i < codeRangesLength; i++) {
            int codeRangeOffset = CodeRange.createCodeRange(dataBuilder,
                subscription.callbackConfig().codeRanges(i).from(),
                subscription.callbackConfig().codeRanges(i).to());
            codeRanges[i] = codeRangeOffset;
        }
        int codeRangesVectorOffset = CallbackConfig.createCodeRangesVector(dataBuilder, codeRanges);

        int callbackConfigOffset = CallbackConfig.createCallbackConfig(dataBuilder, codeRangesVectorOffset);
        int filterOperatorOffset = dataBuilder.createString(subscription.filterOperator());

        int filterKeyValuesLength = subscription.filterKeyValuesListLength();
        int[] filterKeyValues = new int[filterKeyValuesLength];
        for (int i = 0; i < filterKeyValuesLength; i++) {
            int keyOffset = dataBuilder.createString(subscription.filterKeyValuesList(i).key());
            int valuesOffset = dataBuilder.createString(subscription.filterKeyValuesList(i).values());
            FilterKeyValues.createFilterKeyValues(dataBuilder, keyOffset, valuesOffset);
        }

        int filterKeyValuesListOffset = Subscription.createFilterKeyValuesListVector(dataBuilder, filterKeyValues);
        int subscriptionNameOffset = dataBuilder.createString(subscription.name());
        int subscriptionEndpointOffset = dataBuilder.createString(subscription.httpUri());
        int subscriptionHttpMethodOffset = dataBuilder.createString(subscription.httpMethod());
        int subscriptionOffset = Subscription.createSubscription(dataBuilder, id, subscription.id(),
            subscriptionNameOffset, subscription.grouped(), subscription.parallelism(),
            subscription.requestTimeout(), subscription.subscriptionType(), subscription.subscriptionMechanism(),
            subscriptionEndpointOffset, subscriptionHttpMethodOffset, subscription.elastic(), filterOperatorOffset,
            filterKeyValuesListOffset, callbackConfigOffset);
        dataBuilder.finish(subscriptionOffset);
        return Subscription.getRootAsSubscription(dataBuilder.dataBuffer());
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
                return SubscriptionUtils.getPartSubscription(existingSub, partSubscriptionId);
            }
            return null;
        });
    }

    @Override
    public CompletionStage<IPartSubscriber> getPartSubscriber(PartSubscription partSubscription) {
        return CompletableFuture.supplyAsync(() -> {
            log.trace("SubscriberMap contents: {}", subscriberMap);
            log.debug("SubscriberMap status of the part-subscription {} is {}", partSubscription,
                subscriberMap.containsKey(partSubscription));
            // wanted below to work but its creating a new PartSubscriber each
            // time though key is already present
            // subscriberMap.putIfAbsent(partSubscription, new
            // PartSubscriber(partSubscription));

            subscriberMap.computeIfAbsent(partSubscription,
                partSubscription1 -> new PartSubscriber(topicPartDataManager, partSubscription1));
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
        // TODO: fix this method
        String hostPath = "/brokers/" + brokerId + "/subscriptions";
        List<Subscription> subscriptions = new ArrayList<>();
        CompletionStage<List<String>> handleStage = curatorService.getChildren(hostPath)
            .handle((data, exception) -> data);
        List<String> subscriptionIds = handleStage.toCompletableFuture().join();
        for (String id : subscriptionIds) {
            CompletionStage<Subscription> subscriptionStage = getSubscription(Short.valueOf(id.split("-")[0]),
                Short.valueOf(id.split("-")[1]));
            subscriptions.add(subscriptionStage.toCompletableFuture().join());
        }
        return CompletableFuture.supplyAsync(() -> subscriptions);
    }

    @Override
    public CompletionStage<List<Subscription>> getSubscriptionsForTopic(short topicId) {
        String path = config.getTopicsPath() + "/" + topicId + "/subscriptions";
        List<Subscription> subscriptions = new ArrayList<>();
        List<String> subscriptionIds = curatorService.getChildren(path).handle((data, exception) -> data)
            .toCompletableFuture().join();
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
        return topicService.getTopic(subscription.topicId()).handle((data, exception) -> {
            if (exception != null) {
                log.error("Error in get topic stage {}", exception);
                throw new VBrokerException(exception.getMessage());
            } else {
                log.info("Got topic {} with no of partitions {}", data.name(), data.partitions());
                return SubscriptionUtils.getPartSubscriptions(subscription, data.partitions());
            }
        });
    }
}
