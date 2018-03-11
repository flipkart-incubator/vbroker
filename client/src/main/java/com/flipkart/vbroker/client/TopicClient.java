package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 09/03/18.
 */
@Slf4j
@AllArgsConstructor
public class TopicClient {
    private final NetworkClient networkClient;

    public CompletionStage<List<CreateTopicResponse>> createTopics(List<Topic> topics, Node node) {
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

        VRequest vRequest = FlatbufUtils.createVRequest((byte) 1, new Random().nextInt(), protoRequest);

        return networkClient.send(node, vRequest)
            .thenApply(response -> {
                ProtoResponse protoResponse = FlatbufUtils.getProtoResponse(response);
                assert protoResponse.hasCreateTopicsResponse();
                return protoResponse.getCreateTopicsResponse().getCreateTopicsResponseList();
            });
    }
}
