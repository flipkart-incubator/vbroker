package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.DefaultTopicPartDataManager;
import com.flipkart.vbroker.data.TopicPartData;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
public class InMemoryTopicPartDataManager extends DefaultTopicPartDataManager {

    private final Map<TopicPartition, TopicPartData> allPartitionsDataMap = new LinkedHashMap<>();

    @Override
    protected synchronized CompletionStage<TopicPartData> getTopicPartData(TopicPartition topicPartition) {
        CompletableFuture<TopicPartData> future = new CompletableFuture<>();
        allPartitionsDataMap.computeIfAbsent(topicPartition, topicPartition1 -> {
            TopicPartData topicPartData;
            if (topicPartition1.isGrouped()) {
                topicPartData = new InMemoryGroupedTopicPartData();
            } else {
                topicPartData = new InMemoryUnGroupedTopicPartData();
            }
            log.info("TopicPartData: {} for TopicPartition: {}", topicPartData, topicPartition1);
            return topicPartData;
        });
        future.complete(allPartitionsDataMap.get(topicPartition));
        return future;
    }
}
