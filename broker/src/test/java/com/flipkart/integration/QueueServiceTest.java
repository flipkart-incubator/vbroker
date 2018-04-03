package com.flipkart.integration;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.exceptions.QueueCreationException;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.QueueService;
import com.flipkart.vbroker.services.QueueServiceImpl;
import com.flipkart.vbroker.wrappers.Queue;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CompletionStage;

import static org.testng.Assert.*;

public class QueueServiceTest extends BaseClassForTests {

    private CuratorFramework client;
    private CuratorService curatorService;
    private QueueService queueService;

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
        queueService = new QueueServiceImpl(config, curatorService);
        curatorService.createNode("/queues", CreateMode.PERSISTENT, false).toCompletableFuture().get();
        curatorService.createNode("/topics", CreateMode.PERSISTENT, false).toCompletableFuture().get();
    }

    @AfterMethod
    @Override
    public void teardown() throws Exception {
        System.clearProperty("znode.container.checkIntervalMs");
        client.close();
        super.teardown();
    }

    private Queue getQueue() {
        CodeRange codeRange = CodeRange.newBuilder().setFrom(200).setTo(299).build();
        CallbackConfig callbackConfig = CallbackConfig.newBuilder().addCodeRanges(codeRange).build();
        ProtoTopic protoTopic = ProtoTopic.newBuilder().setId(10).setGrouped(true).setName("queue1").setPartitions(1)
            .setReplicationFactor(3).setTopicCategory(TopicCategory.QUEUE).build();
        ProtoSubscription protoSubscription = ProtoSubscription.newBuilder().setId(21).setTopicId(protoTopic.getId())
            .setGrouped(protoTopic.getGrouped()).setSubscriptionType(SubscriptionType.DYNAMIC)
            .setSubscriptionMechanism(SubscriptionMechanism.PUSH).setCallbackConfig(callbackConfig)
            .setHttpUri("http://localhost:12000").setHttpMethod(HttpMethod.POST)
            .setFilterOperator(FilterOperator.NOR).setParallelism(1).setRequestTimeout(60000).setElastic(false)
            .build();
        return new Queue(
            ProtoQueue.newBuilder().setId(1).setTopic(protoTopic).setSubscription(protoSubscription).build());
    }

    @Test
    public void shouldCreateQueue() throws Exception {

        Queue queue = getQueue();
        CompletionStage<Queue> createStage = queueService.createQueueAdmin(queue);
        Queue response = createStage.toCompletableFuture().get();
        assertEquals(queue.topic().name(), response.topic().name());
        assertEquals(queue.id(), response.id());
    }

    @Test
    public void shouldCreateQueue_ButFailOnDuplicateRequest() throws Exception {

        Queue queue = getQueue();
        CompletionStage<Queue> createStage = queueService.createQueueAdmin(queue);
        Queue response = createStage.toCompletableFuture().get();
        assertEquals(queue.topic().name(), response.topic().name());
        assertEquals(queue.id(), response.id());
        queueService.createQueueAdmin(queue).handleAsync((data, exception) -> {
            assertNull(data);
            assertNotNull(exception);
            Throwable ex = exception.getCause();
            assertTrue(ex instanceof QueueCreationException);
            assertTrue(ex.getMessage().contains("NodeExists"));
            return null;
        }).toCompletableFuture().get();

    }

    @Test
    public void shouldGetQueue_AfterCreate() throws Exception {
        Queue queue = getQueue();
        CompletionStage<Queue> createStage = queueService.createQueueAdmin(queue);
        createStage.toCompletableFuture().get();

        CompletionStage<Queue> getStage = queueService.getQueue(1);
        Queue response = getStage.toCompletableFuture().get();

        assertEquals(response.id(), queue.id());
    }
}
