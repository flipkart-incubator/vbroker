package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.TopicService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        Topic topic = topicCreateRequest.topic();
        log.info("Creating topic with id {}, name {}", topic.id(), topic.name());
        return topicService
            .createTopic(topic)
            .thenApplyAsync(createdTopic -> {
                FlatBufferBuilder topicResponseBuilder = new FlatBufferBuilder();
                int status = VStatus.createVStatus(topicResponseBuilder, StatusCode.Success, topicResponseBuilder.createString(""));
                int topicCreateResponse = TopicCreateResponse.createTopicCreateResponse(topicResponseBuilder, createdTopic.id(),
                    status);
                int topicVResponse = VResponse.createVResponse(topicResponseBuilder, 1002, RequestMessage.CreateTopicsRequest,
                    topicCreateResponse);
                topicResponseBuilder.finish(topicVResponse);
                return VResponse.getRootAsVResponse(topicResponseBuilder.dataBuffer());
            });
    }
}