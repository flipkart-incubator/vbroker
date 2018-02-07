package com.flipkart.vbroker.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.core.TopicPartitionDataManager;
import com.flipkart.vbroker.utils.JsonUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hooda on 2/2/18
 */

public class TopicMetadataService {
    private final TopicService topicService;
    private final TopicPartitionDataManager topicPartitionDataManager;
    private final ObjectMapper MAPPER = JsonUtils.getObjectMapper();

    public TopicMetadataService(TopicService topicService,
                                TopicPartitionDataManager topicPartitionDataManager) {
        this.topicService = topicService;
        this.topicPartitionDataManager = topicPartitionDataManager;
    }

    public void saveTopicMetadata(Topic topic) throws IOException {
        Map<String, List<String>> partitionToGroupIdsMap = new HashMap<>();
        for (TopicPartition partition : topic.getPartitions()) {
            List<String> groups = new ArrayList<>(topicPartitionDataManager.getUniqueGroups(partition));
            partitionToGroupIdsMap.put(String.valueOf(partition.getId()), groups);
        }

        File dir = new File("metadata");
        dir.mkdirs();
        File tmp = new File(dir, String.valueOf(topic.getId()).concat(".json"));
        tmp.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tmp));
        bufferedWriter.write(MAPPER.writeValueAsString(partitionToGroupIdsMap));
        bufferedWriter.close();
    }

    public void saveAllTopicMetadata() {
        for (Topic topic : topicService.getAllTopics()) {
            try {
                saveTopicMetadata(topic);
            } catch (IOException ignored) {
            }
        }
    }

    public void fetchTopicMetadata(Topic topic) throws IOException {
        File dir = new File("metadata");
        File tmp = new File(dir, String.valueOf(topic.getId()).concat(".json"));
        TypeReference<HashMap<String, List<String>>> typeRef
                = new TypeReference<HashMap<String, List<String>>>() {
        };

        Map<String, List<String>> partitionToGroupIdsMap = MAPPER.readValue(tmp, typeRef);
        for (TopicPartition partition : topic.getPartitions()) {
            List<String> groupIds = partitionToGroupIdsMap.get(String.valueOf(partition.getId()));
            for (String groupId : groupIds) {
                MessageGroup messageGroup = new MessageGroup(groupId, partition);
                topicPartitionDataManager.addMessageGroup(partition, messageGroup);
            }
        }
    }
}
