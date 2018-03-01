package com.flipkart.vbroker.curator;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.data.memory.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.services.*;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TopicTest {
    VBrokerConfig config;
    CuratorService curatorService;
    TopicService topicService;
    SubscriptionService subscriptionService;

    @BeforeClass
    public void init() throws Exception {
        config = VBrokerConfig.newConfig("broker.properties");
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(config.getZookeeperUrl(),
            new ExponentialBackoffRetry(1000, 5));
        curatorClient.start();
        AsyncCuratorFramework asyncZkClient = AsyncCuratorFramework.wrap(curatorClient);

        curatorService = new CuratorService(asyncZkClient);
        topicService = new TopicServiceImpl(config, curatorService);
        TopicPartDataManager topicPartDataManager = new InMemoryTopicPartDataManager();
        SubPartDataManager subPartDataManager = new InMemorySubPartDataManager(topicPartDataManager);
        subscriptionService = new SubscriptionServiceImpl(config, curatorService, topicPartDataManager, subPartDataManager, topicService);
    }

    @Test
    public void test() throws Exception {
        // Topic topic = DummyEntities.groupedTopic;
        // topicService.createTopic(topic).handleAsync((data, exception) -> {
        // System.out.println("created topic");
        // return null;
        // }).toCompletableFuture().get();

        // subscriptionService.createSubscriptionAdmin((short) 201,
        // DummyEntities.groupedSubscription)
        // .handleAsync((data, exception) -> {
        // System.out.println("created sub");
        // return null;
        // });

        topicService.getAllTopics().handleAsync((data, exc) -> {
            for (Topic t : data) {
                System.out.println(t.id());
                System.out.println(t.name());
            }
            System.out.println(exc);
            return null;
        }).toCompletableFuture().get();

        subscriptionService.getAllSubscriptions().handleAsync((data, exc) -> {
            for (Subscription s : data) {
                System.out.println(s.id());
                System.out.println(s.name());
                System.out.println(s.httpMethod());
            }
            System.out.println(exc);
            return null;
        }).toCompletableFuture().get();
        // subscriptionService.getSubscription((short) 27645, (short)
        // 10502).handleAsync((data, exc) -> {
        // System.out.println(data.name());
        // System.out.println(data.httpUri());
        // return null;
        // }).toCompletableFuture().get();
        //
        // subscriptionService.getSubscriptionsForTopic((short)27645).handleAsync((data,
        // exc) -> {
        // System.out.println(data.get(0).name());
        // System.out.println(data.get(0).httpUri());
        // return null;
        // }).toCompletableFuture().get();

        // topicService.getTopic((short) 101).handleAsync((dat, exc) -> {
        // System.out.println(dat.name());
        // System.out.println(dat.grouped());
        // System.out.println(dat.replicationFactor());
        // return null;
        // });

    }

}
