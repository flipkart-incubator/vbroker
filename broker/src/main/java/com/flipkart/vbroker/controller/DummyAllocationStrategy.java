package com.flipkart.vbroker.controller;

import java.util.List;

import org.apache.zookeeper.CreateMode;

import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        //writes subscriptions allocation to coordinator. Assigns everything to broker 1 for now.
    	log.info("Dummy allocation ongoing...");
    	String path = "/brokers/" + BROKER_ID + "/subscriptions/";
		List<Topic> topics = topicService.getAllTopics();
		for (Topic topic : topics) {
			List<Subscription> subscriptions = subscriptionService.getSubscriptionsForTopic(topic.topicId());
			for (Subscription sub : subscriptions) {
				String nodePath = path + topic.topicId() + "-" + sub.subscriptionId();
				curatorService.createNodeAndSetData(nodePath, CreateMode.PERSISTENT, "".getBytes());
			}
		}
    }

}
