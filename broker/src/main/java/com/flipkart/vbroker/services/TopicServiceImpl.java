package com.flipkart.vbroker.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class TopicServiceImpl implements TopicService {

    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();
    private final VBrokerConfig config;
    private final CuratorService curatorService;

    private final ConcurrentMap<Short, Topic> topicsMap = new ConcurrentHashMap<>();

    @Override
    public synchronized CompletionStage<Topic> createTopic(Topic topic) {
        return CompletableFuture.supplyAsync(() -> {
            topicsMap.putIfAbsent(topic.getId(), topic);
            try {
                curatorService.createNodeAndSetData(config.getTopicsPath() + "/" + topic.getId(), CreateMode.PERSISTENT,
                    topic.toJson().getBytes()).handle((data, exception) -> {
                    if (exception != null) {
                        log.error("Failure in creating topic!");
                    }
                    return null;
                });
            } catch (JsonProcessingException je) {
                throw new VBrokerException("Json serialization failed for topic with id " + topic.getId());
            }
            return topic;
        });
    }

    @Override
    public synchronized void createTopicPartition(Topic topic, TopicPartition topicPartition) {
        topic.addPartition(topicPartition);
        topicsMap.putIfAbsent(topic.getId(), topic);
    }

    @Override
    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, short topicPartitionId) {
        return CompletableFuture.supplyAsync(() -> {
            if (!topicsMap.containsKey(topic.getId())) {
                throw new VBrokerException("Not found topic with id: " + topic.getId());
            }
            Topic existingTopic = topicsMap.get(topic.getId());
            return existingTopic.getPartition(topicPartitionId);
        });
    }

    @Override
    public CompletionStage<Topic> getTopic(short topicId) {
        // return topicsMap.get(topicId);
        return CompletableFuture.supplyAsync(() -> {
            return getTopicByBlocking(topicId);
        });
    }

    private Topic getTopicByBlocking(short topicId) {
        try {
            return curatorService.getData(config.getTopicsPath() + "/" + topicId).handle((data, exception) -> {

                try {
                    return MAPPER.readValue(data, Topic.class);
                } catch (IOException e) {
                    log.error("Error while de-serializing data to Topic.");
                    e.printStackTrace();
                }
                return null;
            }).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while fetching topic from co-ordinator.");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<TopicPartition> getPartitions(Topic topic) {
        return topic.getPartitions();
    }

    @Override
    public CompletionStage<List<Topic>> getAllTopics() {
        return curatorService.getChildren(config.getTopicsPath()).handle((data, exception) -> data)
            .thenComposeAsync(strings -> CompletableFuture.supplyAsync(() -> strings.stream()
                .map(id -> getTopicByBlocking(Short.valueOf(id)))
                .collect(Collectors.toList())));
    }
}
