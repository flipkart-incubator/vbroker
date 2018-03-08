package com.flipkart.integration;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.services.TopicServiceImpl;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Topic;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CompletionStage;

import static org.testng.Assert.assertEquals;

@Slf4j
public class TopicServiceTest extends BaseClassForTests {

    private CuratorFramework client;
    private CuratorService curatorService;
    private TopicService topicService;

    @BeforeMethod
    @Override
    public void setup() throws Exception {
        System.setProperty("znode.container.checkIntervalMs", "1000");
        super.setup();
        client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();
        AsyncCuratorFramework asyncClient = AsyncCuratorFramework.wrap(client);
        curatorService = new CuratorService(asyncClient);
        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        topicService = new TopicServiceImpl(config, curatorService);
    }

    @AfterMethod
    @Override
    public void teardown() throws Exception {
        System.clearProperty("znode.container.checkIntervalMs");
        client.close();
        super.teardown();
    }

    @Test
    public void shouldCreateTopic() throws Exception {
        CompletionStage<Topic> createStage = topicService.createTopic(DummyEntities.groupedTopic);
        Topic topic = createStage.toCompletableFuture().get();
        log.info("Id of created topic is " + topic.id());
        assertEquals(topic.name(), "grouped_topic_1");
        assertEquals(topic.partitions(), (short) 1);
        assertEquals(topic.grouped(), true);
    }

    @Test
    public void topicShouldNotExist() throws Exception {
        CompletionStage<Boolean> stage = topicService.isTopicPresent((short) 1);
        Boolean result = stage.toCompletableFuture().get();
        assertEquals(result, Boolean.FALSE);
    }

    @Test
    public void topicIdShouldExistAfterCreation() throws Exception {
        short id = 101;
        CompletionStage<Topic> createStage = topicService.createTopicAdmin(id, DummyEntities.groupedTopic);
        Topic createdTopic = createStage.toCompletableFuture().get();
        assertEquals(createdTopic.name(), "grouped_topic_1");
        assertEquals(createdTopic.id(), id);
        CompletionStage<Boolean> stage = topicService.isTopicPresent(id);
        Boolean result = stage.toCompletableFuture().get();
        assertEquals(result, Boolean.TRUE);
    }

    @Test
    public void topicNameShouldExistAfterCreation() throws Exception {
        short id = 100;
        CompletionStage<Topic> createStage = topicService.createTopicAdmin(id, DummyEntities.groupedTopic);
        createStage.toCompletableFuture().get();
        CompletionStage<Boolean> existsStage1 = topicService.isTopicPresent("grouped_topic_1");
        CompletionStage<Boolean> existsStage2 = topicService.isTopicPresent("topic2");
        Boolean existsResult1 = existsStage1.toCompletableFuture().get();
        Boolean existsResult2 = existsStage2.toCompletableFuture().get();
        assertEquals(existsResult1, Boolean.TRUE);
        assertEquals(existsResult2, Boolean.FALSE);

    }

    @Test
    public void shouldGetTopic_AfterCreateTopic() throws Exception {
        short id = 101;
        CompletionStage<Topic> createStage = topicService.createTopicAdmin(id, DummyEntities.groupedTopic);
        createStage.toCompletableFuture().get();
        CompletionStage<Topic> getStage = topicService.getTopic(id);
        Topic topic = getStage.toCompletableFuture().get();
        assertEquals(topic.name(), "grouped_topic_1");
        assertEquals(topic.id(), id);
    }

    @Test
    public void shouldGetTopicPartition_AfterCreateTopic() throws Exception {
        short topicId = 101;
        short partitionId = 1;
        CompletionStage<Topic> createStage = topicService.createTopicAdmin(topicId, DummyEntities.groupedTopic);
        Topic topic = createStage.toCompletableFuture().get();
        CompletionStage<TopicPartition> getStage = topicService.getTopicPartition(topic, partitionId);
        TopicPartition partition = getStage.toCompletableFuture().get();
        assertEquals(partition.getId(), partitionId);
        assertEquals(partition.getTopicId(), topicId);
        assertEquals(partition.isGrouped(), true);
    }

}
