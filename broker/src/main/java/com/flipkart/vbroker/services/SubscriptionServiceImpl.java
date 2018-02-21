package com.flipkart.vbroker.services;

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
import com.flipkart.vbroker.utils.SubscriptionUtils;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

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
            .handleAsync((data, exception) -> {
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
        return curatorService.getData(subscriptionPath).handleAsync((data, exception) -> {
            if (exception != null) {
                log.error("Error in getting susbcription data for {}-{}", topicId, subscriptionId);
                throw new VBrokerException("Error while fetching topic subscription");
            } else {
                return Subscription.getRootAsSubscription(ByteBuffer.wrap(data));
            }
        });
    }

    @Override
    public CompletionStage<List<Subscription>> getAllSubscriptionsForBroker(String brokerId) {
        String hostPath = "/brokers/" + brokerId + "/subscriptions";
        List<Subscription> subscriptions = new ArrayList<>();
        return curatorService.getChildren(hostPath)
            .handleAsync((data, exception) -> {
                if (exception != null) {
                    log.error("Error while fetching subscription ids for broker - {}", exception);
                    throw new VBrokerException("Error while fetching subscriptions");
                } else {
                    List<String> subscriptionIds = data;
                    if (nonNull(subscriptionIds)) {
                        List<CompletableFuture> subStages = new ArrayList<CompletableFuture>();
                        log.info("No of subscriptions is {}", subscriptionIds.size());
                        for (String id : subscriptionIds) {
                            String[] arr = id.split("-");
                            if (arr == null || arr.length != 2) {
                                log.error("Unexpected topic-partition id in broker assignment - {}", id);
                                throw new VBrokerException("Invalid id in broker assignment");
                            } else {
                                CompletionStage<Object> subStage = this
                                    .getSubscription(Short.valueOf(arr[0]), Short.valueOf(arr[1]))
                                    .handleAsync((subData, subException) -> {
                                        if (subException != null) {
                                            log.error("Error while fetching subscription data for id {}", id);
                                            throw new VBrokerException("Error while fetching subscription");
                                        } else {
                                            subscriptions.add(subData);
                                            return null;
                                        }
                                    });
                                subStages.add(subStage.toCompletableFuture());
                            }
                        }
                        CompletableFuture<Void> combined = CompletableFuture
                            .allOf((CompletableFuture<?>[]) subStages.toArray());
                        combined.handleAsync((combinedData, combinedException) -> {
                            if (combinedException != null) {
                                log.error("Error while combining futures of subscription fetch {}",
                                    combinedException);
                                throw new VBrokerException("Error while fetching subscriptions");
                            } else {
                                return subscriptions;
                            }
                        });
                    }
                    return subscriptions;
                }
            });
    }

    @Override
    public CompletionStage<List<Subscription>> getSubscriptionsForTopic(short topicId) {
        String path = config.getTopicsPath() + "/" + topicId + "/subscriptions";
        return curatorService.getChildren(path).handleAsync((data, exception) -> {
            if (exception != null) {
                log.error("Error in fetching subscriptions for topic  {}", exception);
                throw new VBrokerException(exception.getMessage());
            } else {
                List<Subscription> subscriptions = new ArrayList<>();
                if (nonNull(data)) {
                    // There are child nodes. Fetch corresponding data for each
                    // and combine to send whole list.
                    List<CompletableFuture> subStages = new ArrayList<CompletableFuture>();
                    for (String id : data) {
                        CompletionStage<Object> stage = this.getSubscription(topicId, Short.valueOf(id))
                            .handleAsync((subData, subException) -> {
                                if (subException != null) {
                                    log.error("Exception while fetching subscription data for {}", id);
                                    throw new VBrokerException("Error while fetching subscription with id " + id);
                                } else {
                                    subscriptions.add(subData);
                                    return null;
                                }
                            });
                        subStages.add(stage.toCompletableFuture());
                    }

                    CompletableFuture<Void> combined = CompletableFuture
                        .allOf((CompletableFuture<?>[]) subStages.toArray());
                    // combining futures to make sure response contains all
                    // data.
                    combined.handleAsync((combinedData, combinedException) -> {
                        if (combinedException != null) {
                            log.error("Error while combining futures of subscription fetch {}", combinedException);
                            throw new VBrokerException("Error while fetching subscriptions");
                        } else {
                            return subscriptions;
                        }
                    });
                } else {
                    return subscriptions;
                }
            }
            // something's wrong if it reaches here.
            return null;
        });
    }

    @Override
    public CompletionStage<List<PartSubscription>> getPartSubscriptions(Subscription subscription) {
        return topicService.getTopic(subscription.topicId()).handleAsync((data, exception) -> {
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
