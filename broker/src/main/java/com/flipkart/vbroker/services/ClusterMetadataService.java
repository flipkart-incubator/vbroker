package com.flipkart.vbroker.services;

import com.flipkart.vbroker.proto.ClusterMetadata;
import com.flipkart.vbroker.proto.Node;

import java.util.concurrent.CompletionStage;

/**
 * Created by kaushal.hooda on 06/03/18.
 */
public interface ClusterMetadataService {

    public CompletionStage<ClusterMetadata> getClusterMetadata();

    public CompletionStage<String> registerNode(Node node);

    CompletionStage<Node> getNode(int id);
}
