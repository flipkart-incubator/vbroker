package com.flipkart.vbroker.controller;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.TopicUtils;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.nonNull;

/**
 * global broker controller which is leader elected across all broker nodes
 */
@Slf4j
public class VBrokerController extends AbstractExecutionThreadService {

    private final CuratorService curatorService;
    private final TopicService topicService;
    private final VBrokerConfig config;

    private final String adminCreateTopicPath;
    private final String adminDeleteTopicPath;
    private final BlockingQueue<WatchedEvent> watchEventsQueue;
    private final CountDownLatch runningLatch = new CountDownLatch(1);
    private volatile AtomicBoolean running = new AtomicBoolean(false);

    public VBrokerController(CuratorService curatorService, TopicService topicService, VBrokerConfig config) {
        this.curatorService = curatorService;
        this.topicService = topicService;
        this.config = config;

        this.adminCreateTopicPath = config.getAdminTasksPath() + "/create_topic";
        this.adminDeleteTopicPath = config.getAdminTasksPath() + "/delete_topic";

        this.watchEventsQueue = new ArrayBlockingQueue<>(config.getControllerQueueSize());
    }

    @Override
    protected void startUp() throws Exception {
        log.info("Setting up controller watches");
        running.set(true);
        watch();
    }

    protected void watch() throws Exception {
        // set watches on admin create_topic task
        CompletionStage<Void> stage = curatorService.watchNodeChildren(adminCreateTopicPath)
            .thenAcceptAsync(watchedEvent -> {
                log.info("Handling {} event", watchedEvent.getType());
                watchEventsQueue.offer(watchedEvent);
            });
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
        curatorService.getChildren(watchedEventPath).thenAcceptAsync(children -> children.forEach(child -> {
            if (adminCreateTopicPath.equalsIgnoreCase(watchedEventPath)) {
                String fullPath = watchedEventPath + "/" + child;
                handleTopicCreation(fullPath, child);
            }
        }));
    }

    private void handleTopicCreation(String fullPath, String nodeName) {
        short topicId = Short.valueOf(nodeName);
        topicService.isTopicPresent(topicId).thenAcceptAsync(isPresent -> {
            if (isPresent) {
                log.error("Topic with id {} already present. Cannot create again. Ignoring", topicId);
            } else {
                curatorService.getData(fullPath)
                    .thenComposeAsync(bytes -> topicService.createTopicAdmin(TopicUtils.getTopic(bytes)));
            }
        }).toCompletableFuture().join(); // make it blocking here
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
