package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.wrappers.Subscription;
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
        return getNodes()
            .thenApply(nodes -> ClusterMetadata.newBuilder().addAllNodes(nodes))
            .thenCompose(builderCompletionStageFunction -> getTopicsMetadata()
                .thenApply(topicMetadata -> builderCompletionStageFunction.addAllTopicMetadatas(topicMetadata).build()));
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
                .map(builder ->
                    getTopicPartitionsMetadata(new Topic(builder.getTopic()))
                        .thenApply(builder::addAllPartitionMetadatas))
                //Add the subscriptions metadata to the builders
                .map(builderStage -> builderStage.thenCompose(builder ->
                    getTopicSubscriptionsMetadata(new Topic(builder.getTopic()))
                        .thenApply(builder::addAllSubscriptionMetadatas)))
                //Finish the builders
                .map(builderStage -> builderStage.thenApply(TopicMetadata.Builder::build))
                .collect(Collectors.toList());
            return CompletionStageUtils.listOfStagesToStageOfList(stages);
        });
    }

    private CompletionStage<List<PartitionMetadata>> getTopicPartitionsMetadata(Topic topic) {
        return CompletionStageUtils.listOfStagesToStageOfList(
            topicService.getPartitions(topic).stream()
                .map(topicPartition -> getPartitionLeader(topicPartition).thenApply(integer -> PartitionMetadata.newBuilder()
                    .setId(topicPartition.getId())
                    .setTopicId(topicPartition.getTopicId())
                    .setLeaderId(integer)
                    .build())
                ).collect(Collectors.toList())
        );
    }

    private CompletionStage<Integer> getPartitionLeader(TopicPartition topicPartition) {
        String path = config.getTopicsPath() + "/" + Integer.toString(topicPartition.getTopicId()) + "/partitions/" + Integer.toString(topicPartition.getId());
        return curatorService.getData(path).thenApply(bytes -> Integer.parseInt(new String(bytes)));
    }

    private CompletionStage<List<SubscriptionMetadata>> getTopicSubscriptionsMetadata(Topic topic) {
        return subscriptionService.getSubscriptionsForTopic(topic.id())
            .thenCompose(subscriptions -> CompletionStageUtils.listOfStagesToStageOfList(
                subscriptions.stream()
                    .map(this::startBuilder)
                    .map(this::addPartSubscriptionMetadataAndBuild)
                    .collect(Collectors.toList())));
    }

    private SubscriptionMetadata.Builder startBuilder(Subscription subscription) {
        try {
            return SubscriptionMetadata.newBuilder()
                .setSubscription(ProtoSubscription.parseFrom(subscription.toBytes()));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletionStage<SubscriptionMetadata> addPartSubscriptionMetadataAndBuild(SubscriptionMetadata.Builder builder) {
        return subscriptionService.getPartSubscriptions(new Subscription(builder.getSubscription()))
            .thenApply(partSubscriptions -> {
                return builder.addAllPartSubscriptionMetadatas(getPartSubscriptionsMetadata(partSubscriptions))
                    .build();
            });
    }

    private List<PartSubscriptionMetadata> getPartSubscriptionsMetadata(List<PartSubscription> partSubscriptions) {
        return partSubscriptions.stream()
            .map(partSubscription -> PartSubscriptionMetadata.newBuilder()
                .setId(partSubscription.getId())
                .setSubscriptionId(partSubscription.getSubscriptionId())
                .setTopicId(partSubscription.getTopicPartition().getTopicId())
                .setTopicPartitionId(partSubscription.getTopicPartition().getId()).build()
            )
            .collect(Collectors.toList());
    }

    private String getBrokerPath(int id) {
        return config.getBrokersPath().concat("/").concat(Integer.toString(id));
    }
}