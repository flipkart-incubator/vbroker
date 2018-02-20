package com.flipkart.vbroker.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.utils.JsonUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Created by hooda on 2/2/18
 */

public class TopicMetadataService {
    private final TopicService topicService;
    private final TopicPartDataManager topicPartDataManager;
    private final ObjectMapper MAPPER = JsonUtils.getObjectMapper();

    public TopicMetadataService(TopicService topicService,
                                TopicPartDataManager topicPartDataManager) {
        this.topicService = topicService;
        this.topicPartDataManager = topicPartDataManager;
    }

    public void saveTopicMetadata(Topic topic) throws IOException {
        Map<String, List<String>> partitionToGroupIdsMap = new HashMap<>();
        for (TopicPartition partition : topicService.getPartitions(topic)) {
            topicPartDataManager.getUniqueGroups(partition).thenAccept(uniqueGroupIds -> {
                List<String> groups = new ArrayList<>(uniqueGroupIds);
                partitionToGroupIdsMap.put(String.valueOf(partition.getId()), groups);
            }).toCompletableFuture().join();
        }

        File dir = new File("metadata");
        dir.mkdirs();
        File tmp = new File(dir, String.valueOf(topic.id()).concat(".json"));
        tmp.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tmp));
        bufferedWriter.write(MAPPER.writeValueAsString(partitionToGroupIdsMap));
        bufferedWriter.close();
    }

    public void saveAllTopicMetadata() {
        CompletionStage<List<Topic>> allTopics = topicService.getAllTopics();
        allTopics.thenAcceptAsync(topics -> {
            topics.forEach(topic1 -> {
                try {
                    saveTopicMetadata(topic1);
                } catch (IOException ignored) {
                }
            });
        });
    }

    public void fetchTopicMetadata(Topic topic) throws IOException {
        File dir = new File("metadata");
        File tmp = new File(dir, String.valueOf(topic.id()).concat(".json"));
        TypeReference<HashMap<String, List<String>>> typeRef
            = new TypeReference<HashMap<String, List<String>>>() {
        };

        Map<String, List<String>> partitionToGroupIdsMap = MAPPER.readValue(tmp, typeRef);
        for (TopicPartition partition : topicService.getPartitions(topic)) {
            List<String> groupIds = partitionToGroupIdsMap.get(String.valueOf(partition.getId()));
            for (String groupId : groupIds) {
                MessageGroup messageGroup = new MessageGroup(groupId, partition);
                topicPartDataManager.addMessageGroup(partition, messageGroup);
            }
        }
    }
}
