package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.exceptions.TopicCreationException;
import com.flipkart.vbroker.exceptions.TopicValidationException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.utils.ByteBufUtils;
import com.flipkart.vbroker.utils.TopicUtils;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Slf4j
@AllArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final VBrokerConfig config;
    private final CuratorService curatorService;

    @Override
    public CompletionStage<Topic> createTopic(Topic topic) throws TopicValidationException {
        if (!validateCreateTopic(topic)) {
            throw new TopicValidationException("Topic create validation failed");
        }

        String topicPath = config.getTopicsPath() + "/" + "0";
        return curatorService.createNodeAndSetData(topicPath, CreateMode.PERSISTENT_SEQUENTIAL,
            ByteBufUtils.getBytes(topic.getByteBuffer())).handleAsync((data, exception) -> {
            if (exception != null) {
                log.error("Exception in curator node create and set data stage {} ", exception);
                throw new TopicCreationException(exception.getMessage());
            } else {
                String arr[] = data.split("/");
                if (!data.contains("/") || arr.length != 2) {
                    log.error("Invalid id {}", data);
                    throw new TopicCreationException("Invalid id data from curator" + data);
                }
                String topicId = arr[1];
                log.info("Created topic with id - " + topicId);
                return newTopicModelFromTopic(Short.valueOf(topicId), topic);
            }
            });
    }

    /**
     * Run validation checks before creating topic.
     *
     * @param topic
     */
    private boolean validateCreateTopic(Topic topic) {
        return true;
        // TODO add validation steps
    }

    /**
     * Returns a new topic created with id and rest of data from topic.
     *
     * @param id
     * @param topic
     * @return
     */
    private Topic newTopicModelFromTopic(short id, Topic topic) {
        FlatBufferBuilder topicBuilder = new FlatBufferBuilder();
        int topicNameOffset = topicBuilder.createString(topic.name());
        int topicOffset = Topic.createTopic(topicBuilder, id, topicNameOffset, topic.grouped(), topic.partitions(),
            topic.replicationFactor(), topic.topicCategory());
        topicBuilder.finish(topicOffset);
        return Topic.getRootAsTopic(topicBuilder.dataBuffer());
    }

    @Override
    public CompletionStage<TopicPartition> getTopicPartition(Topic topic, short topicPartitionId) {
        return getTopic(topic.id())
            .thenApplyAsync(topic1 -> new TopicPartition(topicPartitionId, topic.id(), topic.grouped()));
    }

    @Override
    public CompletionStage<Boolean> isTopicPresent(short topicId) {
        return this.getTopic(topicId).handle((data, exception) -> {
            if (exception != null) {
                log.error("Error while fetchig topic with id {} - {}", topicId, exception);
                throw new VBrokerException(exception.getMessage());
            } else if (data != null) {
                return true;
            }
            return false;
        });
    }

    @Override
    public CompletionStage<Topic> getTopic(short topicId) {
        return curatorService.getData(config.getTopicsPath() + "/" + topicId).handle((data, exception) -> {
            if (exception != null) {
                log.error("Error while fethcing topic with id {} - {}", topicId, exception);
                throw new VBrokerException(exception.getMessage());
            } else {
                return Topic.getRootAsTopic(ByteBuffer.wrap(data));
            }
        });
    }

    @Override
    public List<TopicPartition> getPartitions(Topic topic) {
        return TopicUtils.getTopicPartitions(topic);
    }

    @Override
    public CompletionStage<List<Topic>> getAllTopics() {
        return curatorService.getChildren(config.getTopicsPath()).handle((data, exception) -> {
            if (exception != null) {
                log.error("Error while fethcing topics {}", exception);
                throw new VBrokerException(exception.getMessage());
            } else {
                List<Topic> topics = new ArrayList<Topic>();
                data.forEach((id) -> this.getTopic(Short.valueOf(id)).handle((topicData, topicException) -> {
                    if (topicException != null) {
                        log.error("Error while fethcing topics {}", topicException);
                        throw new VBrokerException(topicException.getMessage());
                    } else {
                        topics.add(topicData);
                        return null;
                    }
                }));
                return topics;
            }
        });
    }

    private boolean checkIfPresent(List<Topic> topics, String name) {
        return topics.stream().anyMatch(topic -> topic.name().equals(name));
    }
}
