package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.DefaultTopicPartDataManager;
import com.flipkart.vbroker.data.TopicPartData;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@ThreadSafe
public class RedisTopicPartDataManager extends DefaultTopicPartDataManager {

    private final RedissonClient client;
    private final ConcurrentMap<TopicPartition, TopicPartData> allPartitionsDataMap = new ConcurrentHashMap<>();

    public RedisTopicPartDataManager(RedissonClient client) {
        this.client = client;
    }

    @Override
    protected CompletionStage<TopicPartData> getTopicPartData(TopicPartition topicPartition) {
        return CompletableFuture.supplyAsync(() -> {
            allPartitionsDataMap.computeIfAbsent(topicPartition, topicPartition1 -> {
                TopicPartData topicPartData;
                if (topicPartition1.isGrouped()) {
                    topicPartData = new RedisGroupedTopicPartData(client, topicPartition1);
                } else {
                    topicPartData = new RedisUnGroupedTopicPartData(client, topicPartition1);
                }
                log.debug("TopicPartData: {} for TopicPartition: {}", topicPartData, topicPartition1);
                return topicPartData;
            });
            return allPartitionsDataMap.get(topicPartition);
        });
    }
}
