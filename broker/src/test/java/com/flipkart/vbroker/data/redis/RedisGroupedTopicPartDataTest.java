package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.utils.TopicUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.concurrent.CompletionStage;

import static org.testng.Assert.*;

public class RedisGroupedTopicPartDataTest {

    private RedisGroupedTopicPartData topicPartData;
    private RedissonClient redissonClient;
    private TopicPartition topicPartition;
    private String groupId;

    @BeforeClass
    public void setUp() {
        TestRedisUtils.startRedisServer();
        topicPartition = TopicUtils.getTopicPartition(DummyEntities.groupedTopic, 0);
        redissonClient = Redisson.create(getRedissonConfig(false));
        topicPartData = new RedisGroupedTopicPartData(redissonClient, topicPartition);
        groupId = "group_0";
    }

    @Test
    public void shouldAddMessage_validateInRedis() {
        Message message = MessageStore.getRandomMsg(groupId);
        CompletionStage<MessageMetadata> stage = topicPartData.addMessage(message);
        MessageMetadata messageMetadata = stage.toCompletableFuture().join();

        assertNotNull(messageMetadata);
        assertEquals(messageMetadata.getMessageId(), message.messageId());
        assertEquals(messageMetadata.getPartitionId(), topicPartition.getId());
    }

    private Config getRedissonConfig(boolean cluster) {
        Config redissonConfig = new Config();
        if (cluster) {
            ClusterServersConfig clusterServersConfig = redissonConfig.useClusterServers()
                .setScanInterval(2000);
            for (String node : TestRedisUtils.getRedisClusterNodes()) {
                clusterServersConfig.addNodeAddress(node);
            }
        } else {
            redissonConfig.useSingleServer().setAddress(TestRedisUtils.getRedisUrl());
        }

        redissonConfig.setCodec(new RedisMessageCodec());
        return redissonConfig;
    }

    @Test
    public void shouldGetUniqueGroups_validateFromRedis() {
        CompletionStage<Set<String>> groupsFuture = topicPartData.getUniqueGroups();
        Set<String> groups = groupsFuture.toCompletableFuture().join();
        assertNotNull(groups);
        assertTrue(groups.contains(groupId));
        assertEquals(1, groups.size());
    }

    @AfterClass
    public void tearDown() {
        TestRedisUtils.stopRedisServer();
        redissonClient.shutdown();
    }
}