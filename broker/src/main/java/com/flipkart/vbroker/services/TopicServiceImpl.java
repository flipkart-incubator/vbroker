package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.exceptions.*;
import com.flipkart.vbroker.utils.ByteBufUtils;
import com.flipkart.vbroker.utils.IdGenerator;
import com.flipkart.vbroker.utils.TopicUtils;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
        short id = IdGenerator.randomId();
        log.info("creating topic request with generated id {}, name {}, rf {}, grouped {}", id, topic.name(), topic.replicationFactor(),
            topic.grouped());
        String topicPath = config.getAdminTasksPath() + "/create_topic" + "/" + id;
        Topic topicWithId = newTopicModelFromTopic(id, topic);
        return curatorService
            .createNodeAndSetData(topicPath, CreateMode.PERSISTENT, ByteBufUtils.getBytes(topicWithId.getByteBuffer()))
            .handleAsync((data, exception) -> {
                if (exception != null) {
                    log.error("Exception in curator node create and set data stage {} ", exception);
                    throw new TopicCreationException(exception.getMessage());
                } else {
                    log.info("Created topic with id - " + id);
                    return newTopicModelFromTopic(Short.valueOf(id), topic);
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
        return this.getTopic(topic.id()).handleAsync((topicData, exception) -> {
            if (exception != null) {
                log.error("Error while getting topic {}", exception);
                throw new TopicException("Error while getting topic " + exception.getMessage());
            }
            return new TopicPartition(topicPartitionId, topic.id(), topic.grouped());
        });
    }

    @Override
    public CompletionStage<Boolean> isTopicPresent(short topicId) {
        return this.getTopic(topicId).handleAsync((data, exception) -> {
            if (exception != null) {
                if (exception.getCause() instanceof TopicNotFoundException) {
                    return false;
                }
                log.error("Error while fetchig topic with id {} - {}", topicId, exception);
                throw new VBrokerException(exception.getMessage());
            } else if (data != null) {
                return true;
            }
            return false;
        });
    }

    @Override
    public CompletionStage<Boolean> isTopicPresent(String name) {
        return this.getAllTopics().handleAsync((topics, exception) -> {
            if (exception != null) {
                throw new VBrokerException("Error while fetching topics");
            } else {
                return checkIfPresent(topics, name);
            }
        });
    }

    @Override
    public CompletionStage<Topic> getTopic(short topicId) {
        return curatorService.getData(config.getTopicsPath() + "/" + topicId).handleAsync((data, exception) -> {
            if (exception != null) {
                if (exception instanceof KeeperException) {
                    if (KeeperException.Code.NONODE.equals(((KeeperException) exception).code())) {
                        log.error("Topic {} does not exist", topicId);
                        throw new TopicNotFoundException();
                    }
                }
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
        List<Topic> topics = new ArrayList<Topic>();
        CompletionStage<Object> combinedStage = curatorService.getChildren(config.getTopicsPath()).thenCompose((topicIds) -> {
            List<CompletableFuture> topicStages = new ArrayList<CompletableFuture>();
            for (String id : topicIds) {
                CompletionStage<Topic> topicStage = this.getTopic(Short.valueOf(id));
                topicStage.handleAsync((topicData, topicExc) -> {
                    topics.add(topicData);
                    return null;
                });
                topicStages.add(topicStage.toCompletableFuture());
            }
            CompletableFuture<Void> combined = CompletableFuture
                .allOf(topicStages.toArray(new CompletableFuture[topicStages.size()]));
            return combined.handleAsync((dat, exc) -> {
                return null;
            });
        });
        return combinedStage.handleAsync((data, exc) -> {
            return topics;
        });
    }

    private boolean checkIfPresent(List<Topic> topics, String name) {
        return topics.stream().anyMatch(topic -> topic.name().equals(name));
    }

    @Override
    public CompletionStage<Topic> createTopicAdmin(short id, Topic topic) throws TopicValidationException {
        log.info("creating topic entity in /topics with id {}, name {}, rf {}, grouped {}", id, topic.name(), topic.replicationFactor(),
            topic.grouped());
        String topicPath = config.getTopicsPath() + "/" + id;
        Topic topicWithId = newTopicModelFromTopic(id, topic);
        return curatorService
            .createNodeAndSetData(topicPath, CreateMode.PERSISTENT, ByteBufUtils.getBytes(topicWithId.getByteBuffer()))
            .handleAsync((data, exception) -> {
                if (exception != null) {
                    log.error("Exception in curator node create and set data stage {} ", exception);
                    throw new TopicCreationException(exception.getMessage());
                } else {
                    log.info("Created topic entity");
                    return newTopicModelFromTopic(id, topic);
                }
            });
    }
}
