package com.flipkart.vbroker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.utils.JsonUtils;
import com.flipkart.vbroker.utils.TopicUtils;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;


@Slf4j
@AllArgsConstructor
public class TopicServiceImpl implements TopicService {

    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();
    private final VBrokerConfig config;
    private final CuratorService curatorService;

    @Override
    public synchronized CompletionStage<Topic> createTopic(Topic topic) {
        return CompletableFuture.supplyAsync(() -> {
            curatorService
                .createNodeAndSetData(config.getTopicsPath() + "/" + topic.id(), CreateMode.PERSISTENT, topic.toBytes())
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
    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, int topicPartitionId) {
        return getTopic(topic.id())
            .thenApplyAsync(topic1 -> new TopicPartition(topicPartitionId, topic.id(), topic.grouped()));
    }

    @Override
    public CompletionStage<Boolean> isTopicPresent(short topicId) {
        return CompletableFuture.supplyAsync(() -> nonNull(getTopicByBlocking(topicId)));
    }

    @Override
    public CompletionStage<Topic> getTopic(int topicId) {
        return CompletableFuture.supplyAsync(() -> getTopicByBlocking(topicId));
    }

    private Topic getTopicByBlocking(int topicId) {
        try {
            return curatorService.getData(config.getTopicsPath() + "/" + topicId).handle((data, exception) -> {
                try {
                    return new Topic(ProtoTopic.parseFrom(data));
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
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
        return TopicUtils.getTopicPartitions(topic);
    }

    @Override
    public CompletionStage<List<Topic>> getAllTopics() {
        return curatorService.getChildren(config.getTopicsPath()).handle((data, exception) -> data)
            .thenComposeAsync(strings -> CompletableFuture.supplyAsync(() -> strings.stream()
                .map(id -> getTopicByBlocking(Short.valueOf(id)))
                .collect(Collectors.toList())));
    }
}
