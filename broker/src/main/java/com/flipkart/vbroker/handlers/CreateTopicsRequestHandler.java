package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.common.util.concurrent.ListeningExecutorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class CreateTopicsRequestHandler implements RequestHandler {

    private final TopicService topicService;
    private final ListeningExecutorService listeningExecutorService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        CreateTopicsRequest createTopicsRequest = FlatbufUtils.getProtoRequest(vRequest).getCreateTopicsRequest();
        List<CompletionStage<CreateTopicResponse>> stages = createTopicsRequest.getTopicsList().stream()
            .map(Topic::new)
            .map(topic -> {
                log.info("Creating topic with id {}, name {}", topic.id(), topic.name());
                return topicService
                    .createTopic(topic)
                    .thenApplyAsync(createdTopic -> {
                        VStatus vStatus = VStatus.newBuilder().setStatusCode(StatusCode.Success).setMessage("").build();
                        return CreateTopicResponse.newBuilder().setName(createdTopic.name()).setStatus(vStatus).build();
                    });
            }).collect(Collectors.toList());

        return CompletionStageUtils.listOfStagesToStageOfList(stages).thenApply(createTopicResponses -> {
            CreateTopicsResponse createTopicsResponse = CreateTopicsResponse
                .newBuilder()
                .addAllCreateTopicsResponse(createTopicResponses)
                .build();
            ProtoResponse protoResponse = ProtoResponse
                .newBuilder()
                .setCreateTopicsResponse(createTopicsResponse)
                .build();
            return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
        });
    }
}