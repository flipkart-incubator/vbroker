package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class ClusterMetadataServiceImpl implements ClusterMetadataService {

    private final CuratorService curatorService;
    private final VBrokerConfig config;
    private final TopicService topicService;
    private final SubscriptionService subscriptionService;


    //TODO think about caching here as well
    public CompletionStage<ClusterMetadata> getClusterMetadata() {
        return null;
    }

    @Override
    public CompletionStage<String> registerNode(Node node) {
        return curatorService.createNodeAndSetData(
            getBrokerPath(node.getBrokerId()),
            CreateMode.EPHEMERAL,
            node.toByteArray(),
            false);
    }

    public CompletionStage<List<Node>> getNodes() {
        return curatorService.getChildren(config.getBrokersPath()).thenCompose(brokerIds -> {
            List<CompletionStage<Node>> stages = brokerIds.stream()
                .map(s -> (Integer.valueOf(s)))
                .map(this::getNode)
                .collect(Collectors.toList());
            return CompletionStageUtils.listOfStagesToStageOfList(stages);
        });
    }

    @Override
    public CompletionStage<Node> getNode(int id) {
        return curatorService.getData(getBrokerPath(id)).thenApply(bytes -> {
            try {
                return Node.parseFrom(bytes);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException("Unable to parse node bytes", e);
            }
        });
    }

    private CompletionStage<List<TopicMetadata>> getTopicsMetadata() {
        return topicService.getAllTopics().thenCompose(topics -> {
            List<CompletionStage<TopicMetadata>> stages = topics.stream()
                //Start a TopicMetadata builder for each topic
                .map(topic -> {
                    TopicMetadata.Builder builder = TopicMetadata.newBuilder();
                    try {
                        return builder.setTopic(ProtoTopic.parseFrom(topic.toBytes()));
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                })
                //Add the partition metadata to the builders
                .map(builder -> {
                    Topic t = new Topic(builder.getTopic());
                    return getTopicPartitionsMetadata(t)
                        .thenApply(builder::addAllPartitionMetadatas);
                })
                //Add the subscriptions metadata to the builders
                .map(builderStage -> builderStage.thenCompose(builder -> {
                    Topic t = new Topic(builder.getTopic());
                    return getTopicSubscriptionsMetadata(t)
                        .thenApply(builder::addAllSubscriptionMetadatas);
                }))
                //Finish the builders
                .map(builderStage -> builderStage.thenApply(TopicMetadata.Builder::build))
                .collect(Collectors.toList());
            return CompletionStageUtils.listOfStagesToStageOfList(stages);
        });
    }

    private CompletionStage<List<PartitionMetadata>> getTopicPartitionsMetadata(Topic topic){
        return null;
    }

    private CompletionStage<List<SubscriptionMetadata>> getTopicSubscriptionsMetadata(Topic topic){
        return null;
    }

    private String getBrokerPath(int id){
        return config.getBrokersPath().concat("/").concat(Integer.toString(id));
    }
}