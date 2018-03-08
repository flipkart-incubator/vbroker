package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.proto.ClusterMetadata;
import com.flipkart.vbroker.proto.Node;
import com.flipkart.vbroker.proto.TopicMetadata;

import java.util.List;
import java.util.stream.Collectors;

public class MetadataServiceImpl {

    private CuratorService curatorService;
    private VBrokerConfig config;

    public ClusterMetadata getClusterMetadata() {
        return ClusterMetadata.newBuilder().addAllNodes(getNodes()).addAllTopicMetadatas(getTopicsMetadata()).build();
    }

    public List<Node> getNodes() {
        curatorService.getChildren("/brokers").handleAsync((brokerIds, exception) -> {
            return brokerIds.stream().map((id -> getNode(id))).collect(Collectors.toList());
        });
        return null;
    }

    private Node getNode(String id) {
        return null;
    }

    public List<TopicMetadata> getTopicsMetadata() {
        return null;
    }
}
