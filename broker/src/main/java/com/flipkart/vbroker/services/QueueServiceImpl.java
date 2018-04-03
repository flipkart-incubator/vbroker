package com.flipkart.vbroker.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.zookeeper.CreateMode;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.exceptions.QueueCreationException;
import com.flipkart.vbroker.exceptions.QueueException;
import com.flipkart.vbroker.proto.ProtoQueue;
import com.flipkart.vbroker.proto.ProtoSubscription;
import com.flipkart.vbroker.proto.ProtoTopic;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.utils.IdGenerator;
import com.flipkart.vbroker.wrappers.Queue;
import com.flipkart.vbroker.wrappers.Subscription;
import com.flipkart.vbroker.wrappers.Topic;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final VBrokerConfig config;
    private final CuratorService curatorService;

    @Override
    public CompletionStage<Queue> getQueue(int queueId) {
        return curatorService.getData(config.getQueuesPath() + "/" + queueId).handleAsync((queueBytes, exception) -> {
            return Queue.fromBytes(queueBytes);
        }).thenComposeAsync((minimalQueue) -> {
            String topicPath = config.getTopicsPath() + "/" + minimalQueue.topic().id();
            String subscriptionPath = topicPath + "/subscriptions/" + minimalQueue.subscription().id();
            CompletionStage<byte[]> topicStage = curatorService.getData(topicPath);
            CompletionStage<byte[]> subStage = curatorService.getData(subscriptionPath);

            return CompletableFuture.allOf(topicStage.toCompletableFuture(), subStage.toCompletableFuture())
                .handleAsync((voidData, combineStageException) -> {
                    try {
                        byte[] topicBytes = topicStage.toCompletableFuture().get();
                        byte[] subBytes = subStage.toCompletableFuture().get();
                        return new Queue(ProtoQueue.newBuilder().setId(minimalQueue.id())
                            .setTopic(Topic.fromBytes(topicBytes).fromTopic())
                            .setSubscription(Subscription.fromBytes(subBytes).fromSubscription())
                            .build());
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Error in completing future", e);
                        throw new QueueException(e.getMessage());
                    }

                });
        }).exceptionally((throwable) -> {
            log.error("Error while composing topic/sub data for queue", throwable);
            throw new QueueException(throwable.getMessage());
        });

    }

    @Override
    public CompletionStage<List<Queue>> getAllQueues() {
        return curatorService.getChildren(config.getQueuesPath()).thenComposeAsync((queueIds) -> {
            List<CompletionStage<Queue>> stages = queueIds.stream()
                .map(id -> this.getQueue(Integer.valueOf(id)).exceptionally(queueThrowable -> {
                    log.error("Error while fetching queue", queueThrowable);
                    throw new QueueException(queueThrowable);
                })).collect(Collectors.toList());
            return CompletionStageUtils.listOfStagesToStageOfList(stages).exceptionally(throwable -> {
                log.error("Error while combining get queue stages", throwable);
                throw new QueueException(throwable);
            });
        });
    }

    /**
     * Creates and returns a clone of queue, but with ids set.
     *
     * @param queue
     * @param id
     * @param topicId
     * @param subscriptionId
     * @return
     */
    private Queue getQueueWithIds(Queue queue, int id, int topicId, int subscriptionId) {
        return new Queue(queue.fromQueue().setTopic(queue.topic().fromTopic().setId(topicId))
            .setSubscription(queue.subscription().fromSubscription().setId(subscriptionId)).setId(id)
            .build());
    }

    /**
     * Creates and returns a new queue entity with just the ids populated.
     *
     * @param queue
     * @param id
     * @param topicId
     * @param subscriptionId
     * @return
     */
    private Queue getMinimalQueue(Queue queue, int id, int topicId, int subscriptionId) {
        return new Queue(ProtoQueue.newBuilder().setId(id).setTopic(ProtoTopic.newBuilder().setId(topicId).build())
            .setSubscription(ProtoSubscription.newBuilder().setId(subscriptionId).build()).build());
    }

    @Override
    public CompletionStage<Queue> createQueue(Queue queue) {
        int id = IdGenerator.randomQueueId();
        int topicId = IdGenerator.randomTopicId();
        int subscriptionId = IdGenerator.randomSubscriptionId();
        String queueAdminPath = config.getAdminTasksPath() + "/create_queue" + "/" + id;
        Queue queueWithIds = getQueueWithIds(queue, id, topicId, subscriptionId);
        return curatorService.createNodeAndSetData(queueAdminPath, CreateMode.PERSISTENT, queueWithIds.toBytes(), false)
            .handleAsync((data, exception) -> {
                if (exception != null) {
                    log.error("Exception in curator createAndSet stage", exception);
                    throw new QueueCreationException(exception.getMessage());
                }
                log.info("Created queue request for admin");
                return queueWithIds;
            });
    }

    public CompletionStage<Queue> createQueueAdmin(Queue queue) {
        int id = queue.id();
        int topicId = queue.topic().id();
        int subscriptionId = queue.subscription().id();
        Queue minimalQueue = getMinimalQueue(queue, id, topicId, subscriptionId);
        List<CuratorCreateOp> curatorCreateOps = new ArrayList<>();
        curatorCreateOps.add(new CuratorCreateOp(config.getQueuesPath() + "/" + id, minimalQueue.toBytes()));
        curatorCreateOps.add(new CuratorCreateOp(config.getTopicsPath() + "/" + topicId, queue.topic().toBytes()));
        curatorCreateOps.add(new CuratorCreateOp(config.getTopicsPath() + "/" + topicId + "/subscriptions", "".getBytes()));
        curatorCreateOps
            .add(new CuratorCreateOp(config.getTopicsPath() + "/" + topicId + "/subscriptions/" + subscriptionId,
                queue.subscription().toBytes()));
        return curatorService.createInTransaction(curatorCreateOps).handleAsync((transactionResults, exception) -> {
            if (exception != null) {
                log.error("Exception in curator createAndSet stage", exception);
                throw new QueueCreationException(exception.getMessage());
            }
            log.info("Processing CreateQueue transaction result.");
            List<CuratorTransactionResult> errored = transactionResults.stream().filter(res -> res.getError() != 0)
                .collect(Collectors.toList());
            if (!errored.isEmpty()) {
                // TODO: check/validate if handling partial success is required.
                log.error("Failure in transaction. {} ops failed. ", errored.size());
                errored.forEach(op -> log.error("Path - {}, Error - {}", op.getForPath(), op.getError()));
                throw new QueueCreationException("Some ops in queue-creation transaction failed");
            }
            log.info("Created queue entity via transaction successfully");
            return queue;
        });
    }
}
