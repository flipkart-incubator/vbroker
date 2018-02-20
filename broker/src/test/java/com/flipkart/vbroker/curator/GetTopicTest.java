package com.flipkart.vbroker.curator;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.subscribers.DummyEntities;
import com.flipkart.vbroker.utils.ByteBufUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

    @Test
    public void createTopicTest() throws Exception {
        String topicPath = config.getTopicsPath() + "/" + "0";
        Topic topic = DummyEntities.topic1;
        curatorService.createNodeAndSetData(topicPath, CreateMode.PERSISTENT_SEQUENTIAL,
            ByteBufUtils.getBytes(topic.getByteBuffer())).handleAsync((data, exception) -> {
            System.out.println("created " + data);
            return null;
        });
    }
}
