package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.exceptions.SubscriptionCreationException;
import com.flipkart.vbroker.exceptions.SubscriptionException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.subscribers.GroupedPartSubscriber;
import com.flipkart.vbroker.subscribers.PartSubscriber;
import com.flipkart.vbroker.subscribers.UnGroupedPartSubscriber;
import com.flipkart.vbroker.utils.IdGenerator;
import com.flipkart.vbroker.utils.SubscriptionUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import com.flipkart.vbroker.wrappers.Topic;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final VBrokerConfig config;
    private final CuratorService curatorService;
    private final TopicPartDataManager topicPartDataManager;
    private final SubPartDataManager subPartDataManager;
    private final TopicService topicService;

    @Override
    public CompletionStage<Subscription> createSubscription(Subscription subscription) {
        short id = IdGenerator.randomId();
        String path = config.getAdminTasksPath() + "/create_subscription" + "/" + id;
        log.info("Creating subscription with id {} for topicId {} with uri {}", id, subscription.topicId(),
            subscription.httpUri());
        return curatorService.createNodeAndSetData(path, CreateMode.PERSISTENT,
            subscription.toBytes(), false).handleAsync((data, exception) -> {
            if (exception != null) {
                log.error("Exception in curator node create and set data stage {} ", exception);
                throw new SubscriptionCreationException(exception.getMessage());
            } else {
                log.info("Created subscription with id - " + id);
                return subscription;
            }
        });
    }

    @Override
    public CompletionStage<List<Subscription>> getAllSubscriptions() {
        List<Subscription> subs = new ArrayList<>();
        CompletionStage<Object> com = topicService.getAllTopics().thenCompose((topics) -> {
            List<CompletableFuture> subStages = new ArrayList<CompletableFuture>();
            for (Topic topic : topics) {
                CompletionStage<List<Subscription>> subStage = this.getSubscriptionsForTopic(topic.id());
                CompletionStage<List<Subscription>> subComStage = subStage.thenCompose((subData) -> {
                    subs.addAll(subData);
                    return CompletableFuture.completedFuture(subData);
                }).exceptionally((throwable) -> {
                    log.error("Exception in fetching subs stage", throwable);
                    throw new SubscriptionException("Execption while fetching all subscriptions");
                });

                subStages.add(subComStage.toCompletableFuture());
            }
            CompletableFuture<Void> combined = CompletableFuture
                .allOf(subStages.toArray(new CompletableFuture[subStages.size()]));
            return combined.handleAsync((dat, exc) -> {
                return null;
            }).exceptionally((throwable) -> {
                log.error("Exception in combine stage", throwable);
                throw new SubscriptionException("Execption while fetching all subscriptions");
            });
        });
        return com.handleAsync((data, exc) -> {
            return subs;
        }).exceptionally((throwable) -> {
            log.error("Exception in combine stage handling", throwable);
            throw new SubscriptionException("Execption while fetching all subscriptions");
        });
    }

    @Override
    public CompletionStage<PartSubscription> getPartSubscription(Subscription subscription, int partSubscriptionId) {
        return this.getSubscription(subscription.topicId(), subscription.id())
            .handleAsync((existingSubscription, exception) -> {
                if (exception != null) {
                    log.error("Error while fetching subscription with topicId {} and subId {}",
                        subscription.topicId(), subscription.id());
                    throw new SubscriptionException("Error while fetching subscription");
                }
                return SubscriptionUtils.getPartSubscription(existingSubscription, partSubscriptionId);
            });
    }

    @Override
    public CompletionStage<PartSubscriber> getPartSubscriber(PartSubscription partSubscription) {
        return this.getSubscription(partSubscription.getTopicPartition().getTopicId(),
            partSubscription.getSubscriptionId()).handleAsync((subscription, exception) -> {
            if (exception != null) {
                log.error("Error while fethcing sub with topicId {} and id {}",
                    partSubscription.getTopicPartition().getTopicId(),
                    partSubscription.getSubscriptionId());
                throw new SubscriptionException("Error while fetching subscription");
            } else {
                PartSubscriber partSubscriber;
                if (partSubscription.isGrouped()) {
                    partSubscriber = new GroupedPartSubscriber(topicPartDataManager, subPartDataManager,
                        partSubscription);
                } else {
                    partSubscriber = new UnGroupedPartSubscriber(subPartDataManager, partSubscription);
                }
                return partSubscriber;
            }
        });
    }

    @Override
    public CompletionStage<Subscription> getSubscription(int topicId, int subscriptionId) {
        String subscriptionPath = config.getTopicsPath() + "/" + topicId + "/subscriptions/" + subscriptionId;
        return curatorService.getData(subscriptionPath).handleAsync((data, exception) -> {
            if (exception != null) {
                log.error("Error in getting susbcription data for {}-{}", topicId, subscriptionId);
                throw new VBrokerException("Error while fetching topic subscription");
            } else {
                return Subscription.fromBytes(data);
            }
        });
    }

    @Override
    public CompletionStage<List<Subscription>> getAllSubscriptionsForBroker(String brokerId) {
        String hostPath = "/brokers/" + brokerId + "/subscriptions";
        List<Subscription> subscriptions = new ArrayList<>();
        return curatorService.getChildren(hostPath).handleAsync((data, exception) -> {
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
                            log.error("Error while combining futures of subscription fetch {}", combinedException);
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

    private CompletionStage<List<Subscription>> getSubscriptionsForIds(short topicId, List<String> ids) {
        CompletableFuture[] stages = new CompletableFuture[ids.size()];
        List<Subscription> subs = new ArrayList<>();
        int j = 0;
        for (String id : ids) {
            stages[j] = this.getSubscription(topicId, Short.valueOf(id)).toCompletableFuture();
            j++;
        }
        return CompletableFuture.allOf(stages).handleAsync((data, exception) -> {
            for (int i = 0; i < stages.length; i++) {
                Subscription sub;
                try {
                    sub = (Subscription) stages[i].toCompletableFuture().get();
                    subs.add(sub);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return subs;
        });

    }

    @Override
    public CompletionStage<List<Subscription>> getSubscriptionsForTopic(int topicId) {
        String path = config.getTopicsPath() + "/" + topicId + "/subscriptions";

        return curatorService.getChildren(path).thenCompose((children) -> {
            List<CompletionStage<Subscription>> subscriptionsStages = children.stream()
                .map(child -> getSubscription(topicId, Short.valueOf(child)).exceptionally(throwable -> {
                    log.error("Exception while fetching subscription data for {}", child);
                    throw new SubscriptionException("Error while fetching subscription with id {}" + child);
                })).collect(Collectors.toList());

            @SuppressWarnings("SuspiciousToArrayCall")
            CompletableFuture<Void> allStages = CompletableFuture
                .allOf(subscriptionsStages.toArray(new CompletableFuture[subscriptionsStages.size()]));

            return allStages.thenApply(aVoid -> subscriptionsStages.stream()
                .map(stage -> stage.toCompletableFuture().join()).collect(Collectors.toList()));
        });

    }

    @Override
    public CompletionStage<List<PartSubscription>> getPartSubscriptions(Subscription subscription) {
        return topicService.getTopic(subscription.topicId()).handleAsync((topic, exception) -> {
            if (exception != null) {
                log.error("Error in get topic stage {}", exception);
                throw new VBrokerException(exception.getMessage());
            } else {
                log.info("Got topic {} with no of partitions {}", topic.name(), topic.partitions());
                return SubscriptionUtils.getPartSubscriptions(subscription, topic.partitions());
            }
        });
    }

    @Override
    public CompletionStage<Subscription> createSubscriptionAdmin(short id, Subscription subscription) {
        log.info("Call from controller to create subscription with id {} name {}", id, subscription.name());
        String path = config.getTopicsPath() + "/" + subscription.topicId() + "/subscriptions/" + id;
        return curatorService.createNodeAndSetData(path, CreateMode.PERSISTENT,
            subscription.toBytes(), false).handleAsync((data, exception) -> {
            if (exception != null) {
                log.error("Exception in curator node create and set data stage {} ", exception);
                throw new SubscriptionCreationException(exception.getMessage());
            } else {
                log.info("Created subscription entity with id - " + id);
                return subscription;
            }
        });
    }

    @Override
    public CompletionStage<Integer> getPartSubscriptionLag(PartSubscription partSubscription) {
        return subPartDataManager.getLag(partSubscription);
    }
}
