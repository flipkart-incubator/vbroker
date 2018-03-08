package com.flipkart.vbroker.controller;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.TopicUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.nonNull;

/**
 * global broker controller which is leader elected across all broker nodes
 */
@Slf4j
public class VBrokerController extends AbstractExecutionThreadService {

    private final CuratorService curatorService;
    private final TopicService topicService;
    private final SubscriptionService subscriptionService;
    private final VBrokerConfig config;

    private final String adminCreateTopicPath;
    private final String adminDeleteTopicPath;
    private final String adminCreateSubscriptionPath;
    private final BlockingQueue<WatchedEvent> watchEventsQueue;
    private final CountDownLatch runningLatch = new CountDownLatch(1);
    private final ChildrenCache topics = new ChildrenCache(new ArrayList());
    private final ChildrenCache subscriptions = new ChildrenCache(new ArrayList());
    private volatile AtomicBoolean running = new AtomicBoolean(false);

    public VBrokerController(CuratorService curatorService, TopicService topicService,
                             SubscriptionService subscriptionService, VBrokerConfig config) {
        this.curatorService = curatorService;
        this.topicService = topicService;
        this.subscriptionService = subscriptionService;
        this.config = config;

        this.adminCreateTopicPath = config.getAdminTasksPath() + "/create_topic";
        this.adminDeleteTopicPath = config.getAdminTasksPath() + "/delete_topic";
        this.adminCreateSubscriptionPath = config.getAdminTasksPath() + "/create_subscription";

        this.watchEventsQueue = new ArrayBlockingQueue<>(config.getControllerQueueSize());
    }

    @Override
    protected void startUp() throws Exception {
        log.info("Setting up controller watches");
        running.set(true);
        watch();
    }

    protected void watch() {
        curatorService.watchNodeChildren(adminCreateTopicPath).thenAcceptAsync(watchedEvent -> {
            handleWatchedEvent(adminCreateTopicPath, watchedEvent);
        });
        curatorService.watchNodeChildren(adminCreateSubscriptionPath).thenAcceptAsync(watchedEvent -> {
            handleWatchedEvent(adminCreateSubscriptionPath, watchedEvent);
        });
    }

    private void handleWatchedEvent(String path, WatchedEvent watchedEvent) {
        log.info("Handling {} event", watchedEvent.getType());
        watchEventsQueue.offer(watchedEvent);
        watch();
    }

    @Override
    protected void run() throws Exception {
        while (this.running.get()) {
            WatchedEvent watchedEvent = watchEventsQueue.poll(config.getControllerQueuePollTimeMs(),
                TimeUnit.MILLISECONDS);
            if (nonNull(watchedEvent)) {
                handleWatchEvent(watchedEvent);
            }
        }
        runningLatch.countDown();
    }

    /**
     * this can be a blocking operation
     *
     * @param watchedEvent to handle
     */
    public void handleWatchEvent(WatchedEvent watchedEvent) {
        log.info("Handling WatchedEvent {}", watchedEvent);

        String watchedEventPath = watchedEvent.getPath();
        // TODO: model this as commands
        switch (watchedEvent.getType()) {
            case NodeChildrenChanged:
                handleNodeChildrenChanged(watchedEventPath);
                break;
            case None:
            case NodeCreated:
            case NodeDeleted:
            case NodeDataChanged:
            default:
                log.info("Unsupported watchedEvent type {}. Ignoring", watchedEvent.getType());
                break;
        }
    }

    private void handleNodeChildrenChanged(String watchedEventPath) {
        curatorService.getChildren(watchedEventPath).thenAcceptAsync(children -> {
            if (adminCreateTopicPath.equalsIgnoreCase(watchedEventPath)) {
                List<String> newChildren = children;
                List<String> newTopics = topics.diffAndSet(newChildren);
                for (String topicId : newTopics) {
                    String fullPath = watchedEventPath + "/" + topicId;
                    handleTopicCreation(fullPath, topicId);
                }
            } else if (adminCreateSubscriptionPath.equalsIgnoreCase(watchedEventPath)) {
                List<String> newChildren = children;
                List<String> newSubs = subscriptions.diffAndSet(newChildren);
                for (String subId : newSubs) {
                    String fullPath = watchedEventPath + "/" + subId;
                    handleSubscriptionCreation(fullPath, subId);
                }
            }
        });
    }

    private void handleSubscriptionCreation(String fullPath, String child) {
        short subscriptionId = Short.valueOf(child);
        curatorService.getData(fullPath).thenComposeAsync(bytes -> subscriptionService
            .createSubscriptionAdmin(subscriptionId, Subscription.fromBytes(bytes)))
            .toCompletableFuture().join();

    }

    private void handleTopicCreation(String fullPath, String nodeName) {
        short topicId = Short.valueOf(nodeName);
        topicService.isTopicPresent(topicId).thenAcceptAsync(isPresent -> {
            if (isPresent) {
                log.error("Topic with id {} already present. Cannot create again. Ignoring", topicId);
            } else {
                curatorService.getData(fullPath)
                    .thenComposeAsync(bytes -> topicService.createTopicAdmin(topicId, TopicUtils.getTopic(bytes)));
            }
        }).toCompletableFuture().join();
    }

    @Override
    protected void triggerShutdown() {
        // TODO: cleanup watches first
        running.set(false);
        try {
            runningLatch.await();
        } catch (InterruptedException ignored) {
        }
    }
}
