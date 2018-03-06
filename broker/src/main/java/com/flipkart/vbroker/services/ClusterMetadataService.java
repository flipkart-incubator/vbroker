package com.flipkart.vbroker.services;

import com.flipkart.vbroker.proto.ClusterMetadata;

import java.util.concurrent.CompletionStage;

/**
 * Created by kaushal.hooda on 06/03/18.
 */
public interface ClusterMetadataService {

    public CompletionStage<ClusterMetadata> getClusterMetadata();
}
