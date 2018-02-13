package com.flipkart.vbroker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.utils.JsonUtils;
import com.flipkart.vbroker.utils.TopicUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Slf4j
@AllArgsConstructor
public class TopicServiceImpl implements TopicService {

    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();
    private final VBrokerConfig config;
    private final CuratorService curatorService;

    @Override
    public synchronized CompletionStage<Topic> createTopic(Topic topic) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] bytes = new byte[topic.getByteBuffer().remaining()];
            topic.getByteBuffer().get(bytes);
            curatorService
                .createNodeAndSetData(config.getTopicsPath() + "/" + topic.topicId(), CreateMode.PERSISTENT, bytes)
                .handle((data, exception) -> {
                    if (exception != null) {
                        log.error("Failure in creating topic!");
                    }
                    return null;
                });
            return topic;
        });
    }

    @Override
    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, short topicPartitionId) {
        return getTopic(topic.topicId())
            .thenApplyAsync(topic1 -> new TopicPartition(topicPartitionId, topic.topicId()));
    }

    @Override
    public CompletionStage<Topic> getTopic(short topicId) {
        return CompletableFuture.supplyAsync(() -> getTopicByBlocking(topicId));
    }

    private Topic getTopicByBlocking(short topicId) {
        try {
            return curatorService.getData(config.getTopicsPath() + "/" + topicId).handle((data, exception) -> {
                return Topic.getRootAsTopic(ByteBuffer.wrap(data));
                // return MAPPER.readValue(data, Topic.class);
            }).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while fetching topic from coordinator.");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<TopicPartition> getPartitions(Topic topic) {
        return TopicUtils.getTopicPartitions(topic.topicId(), topic.partitions());
    }

    @Override
    public CompletionStage<List<Topic>> getAllTopics() {
        return curatorService.getChildren(config.getTopicsPath()).handle((data, exception) -> data)
            .thenComposeAsync(strings -> CompletableFuture.supplyAsync(() -> strings.stream()
                .map(id -> getTopicByBlocking(Short.valueOf(id)))
                .collect(Collectors.toList())));
    }
}
