package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 09/03/18.
 */
@Slf4j
@AllArgsConstructor
public class TopicClient {
    private final NetworkClient networkClient;
    private final Metadata metadata;

    public CompletionStage<List<CreateTopicResponse>> createTopics(List<Topic> topics) {
        List<ProtoTopic> protoTopics = topics.stream()
            .map(topic -> {
                try {
                    return ProtoTopic.parseFrom(topic.toBytes());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
        CreateTopicsRequest topicsRequest = CreateTopicsRequest.newBuilder()
            .addAllTopics(protoTopics)
            .build();

        ProtoRequest protoRequest = ProtoRequest.newBuilder()
            .setCreateTopicsRequest(topicsRequest)
            .build();

        VRequest vRequest = FlatbufUtils.createVRequest(protoRequest);

        return networkClient.send(getNode(), vRequest)
            .thenApply(response -> {
                ProtoResponse protoResponse = FlatbufUtils.getProtoResponse(response);
                assert protoResponse.hasCreateTopicsResponse();
                return protoResponse.getCreateTopicsResponse().getCreateTopicsResponseList();
            });
    }

    public CompletionStage<List<GetTopicResponse>> getTopics(List<Integer> topicIds){
        GetTopicsRequest getTopicsRequest = GetTopicsRequest.newBuilder()
            .addAllIds(topicIds)
            .build();

        ProtoRequest protoRequest = ProtoRequest.newBuilder()
            .setGetTopicsRequest(getTopicsRequest)
            .build();

        VRequest vRequest = FlatbufUtils.createVRequest(protoRequest);

        return networkClient.send(getNode(), vRequest).thenApply(response -> {
            ProtoResponse protoResponse = FlatbufUtils.getProtoResponse(response);
            assert protoResponse.hasGetTopicsResponse();
            return protoResponse.getGetTopicsResponse().getTopicResponsesList();
        });
    }

    private Node getNode() {
        return metadata.getClusterNodes().get(0);
    }
}
