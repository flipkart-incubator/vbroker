package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.TopicService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class TopicCreateRequestHandler implements RequestHandler {

    private final TopicService topicService;
    private final ListeningExecutorService listeningExecutorService;

    @Override
    public ListenableFuture<VResponse> handle(VRequest vRequest) {
        TopicCreateRequest topicCreateRequest = (TopicCreateRequest) vRequest.requestMessage(new TopicCreateRequest());
        assert nonNull(topicCreateRequest);

        Topic topic = Topic.TopicBuilder.aTopic().withGrouped(topicCreateRequest.grouped())
                .withId(topicCreateRequest.topicId()).withName(topicCreateRequest.topicName())
                .withNoOfPartitions(topicCreateRequest.partitions())
                .withReplicationFactor(topicCreateRequest.replicationFactor()).withTeam(topicCreateRequest.team())
                .withTopicCategory(Topic.TopicCategory
                        .valueOf(TopicCategory.name(topicCreateRequest.topicCategory())))
                .build();

        return listeningExecutorService.submit(() -> {
            log.info("Creating topic with id {}, name {}", topic.getId(), topic.getName());
            topicService.createTopic(topic);

            FlatBufferBuilder topicResponseBuilder = new FlatBufferBuilder();
            int topicCreateResponse = TopicCreateResponse.createTopicCreateResponse(topicResponseBuilder, topic.getId(),
                    (short) 200);
            int topicVResponse = VResponse.createVResponse(topicResponseBuilder, 1002, RequestMessage.TopicCreateRequest,
                    topicCreateResponse);
            topicResponseBuilder.finish(topicVResponse);
            return VResponse.getRootAsVResponse(topicResponseBuilder.dataBuffer());
        });
    }
}