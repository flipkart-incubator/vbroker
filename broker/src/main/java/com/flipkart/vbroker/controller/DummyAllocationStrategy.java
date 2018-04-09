package com.flipkart.vbroker.controller;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.wrappers.Topic;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * @author govind.ajith
 */
@Slf4j
@AllArgsConstructor
public class DummyAllocationStrategy implements AllocationStrategy {

    private static final String BROKER_ID = "1";
    private final TopicService topicService;
    private final SubscriptionService subscriptionService;
    private final CuratorService curatorService;

    @Override
    public void allocate() {
        //writes topics/subscriptions allocation to coordinator. Assigns everything to broker 1 for now.
        log.info("Dummy allocation ongoing...");
        String subPath = "/brokers/" + BROKER_ID + "/subscriptions/";
        CompletionStage<List<Topic>> topicStage = topicService.getAllTopics();
        topicStage.thenAcceptAsync(topics -> {
            topics.forEach(topic -> {
                topicService.getPartitions(topic).stream().forEach(topicPartition -> {
                    String path = "/topics/" + Integer.toString(topic.id()) + "/partitions/" + Integer.toString(topicPartition.getId());
                    curatorService.createNodeAndSetData(path, CreateMode.PERSISTENT, BROKER_ID.getBytes(), true);
                });
//                subscriptionService.getSubscriptionsForTopic(topic.id()).thenAcceptAsync(subscriptions -> {
//                    subscriptions.forEach(subscription -> {
//                        String nodePath = subPath + topic.id() + "-" + subscription.id();
//                        curatorService.createNodeAndSetData(nodePath, CreateMode.PERSISTENT, "".getBytes(), true);
//                    });
//                });
            });
        });
    }

}
