package com.flipkart.vbroker.curator;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Topic;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.testng.annotations.BeforeClass;

public class GetTopicTest {

    VBrokerConfig config;
    CuratorService curatorService;

    @BeforeClass
    public void init() throws Exception {
        config = VBrokerConfig.newConfig("broker.properties");
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(config.getZookeeperUrl(),
            new ExponentialBackoffRetry(1000, 5));
        curatorClient.start();
        AsyncCuratorFramework asyncZkClient = AsyncCuratorFramework.wrap(curatorClient);

        curatorService = new CuratorService(asyncZkClient);
    }

    //@Test
    public void createTopicTest() throws Exception {
        String topicPath = config.getTopicsPath() + "/" + "101";
        Topic topic = DummyEntities.groupedTopic;
        curatorService.createNodeAndSetData(topicPath, CreateMode.PERSISTENT,
            topic.toBytes(), true).handleAsync((data, exception) -> {
            System.out.println("created " + data);
            return null;
        }).toCompletableFuture().get();
    }

    //@Test
    public void testGet() throws Exception {

        curatorService.getData("/topics/101").handleAsync((data, exception) -> {
            Topic t = Topic.fromBytes(data);
            System.out.println(t.name());
            System.out.println(t.partitions());
            System.out.println(t.topicCategory());
            return null;
        }).toCompletableFuture().join();

//		curatorService.getData("/admin/create_subscription/31989").handleAsync((data, exception) -> {
//			Subscription s = SubscriptionUtils.getSubscription(data);
//			System.out.println(s.topicId());
//			System.out.println(s.httpMethod());
//			System.out.println(s.httpUri());
//			return null;
//		}).toCompletableFuture().join();

    }
}
