package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.TopicService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class TopicCreateRequestHandler implements RequestHandler {

    private final TopicService topicService;
    private final ListeningExecutorService listeningExecutorService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        TopicCreateRequest topicCreateRequest = (TopicCreateRequest) vRequest.requestMessage(new TopicCreateRequest());
        assert nonNull(topicCreateRequest);
        List<TopicPartition> partitions = new ArrayList<>();
        for (int i = 0; i < topicCreateRequest.topic().partitions(); i++) {
            partitions.add(new TopicPartition((short) i, topicCreateRequest.topic().topicId()));
        }

        Topic topic = Topic.TopicBuilder.aTopic().withGrouped(topicCreateRequest.topic().grouped())
            .withId(topicCreateRequest.topic().topicId()).withName(topicCreateRequest.topic().topicName())
            .withNoOfPartitions(topicCreateRequest.topic().partitions())
            .withReplicationFactor(topicCreateRequest.topic().replicationFactor()).withPartitions(partitions)
            .build();

        log.info("Creating topic with id {}, name {}", topic.getId(), topic.getName());
        return topicService
            .createTopic(topic)
            .thenApplyAsync(createdTopic -> {
                FlatBufferBuilder topicResponseBuilder = new FlatBufferBuilder();
                int status = VStatus.createVStatus(topicResponseBuilder, StatusCode.Success, topicResponseBuilder.createString(""));
                int topicCreateResponse = TopicCreateResponse.createTopicCreateResponse(topicResponseBuilder, createdTopic.getId(),
                    status);
                int topicVResponse = VResponse.createVResponse(topicResponseBuilder, 1002, RequestMessage.TopicCreateRequest,
                    topicCreateResponse);
                topicResponseBuilder.finish(topicVResponse);
                return VResponse.getRootAsVResponse(topicResponseBuilder.dataBuffer());
            });
    }
}