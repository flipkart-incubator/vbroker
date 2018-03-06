package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Created by kaushal.hooda on 05/03/18.
 */
@Slf4j
@AllArgsConstructor
public class GetTopicsRequestHandler implements RequestHandler {
    private final TopicService topicService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        GetTopicsRequest getTopicsRequest = FlatbufUtils.getProtoRequest(vRequest).getGetTopicsRequest();

        List<CompletionStage<GetTopicResponse>> stages = new ArrayList<>();
        for (Integer topicId : getTopicsRequest.getIdsList()) {
            CompletionStage<Topic> topicCompletionStage = topicService.getTopic(topicId);
            CompletionStage<GetTopicResponse> getTopicResponseCompletionStage = handleTopicStage(topicCompletionStage, topicId);
            stages.add(getTopicResponseCompletionStage);
        }

        return CompletionStageUtils.listOfStagesToStageOfList(stages).thenApply(getTopicResponses -> {
            GetTopicsResponse getTopicsResponse = GetTopicsResponse.newBuilder().addAllTopicResponses(getTopicResponses).build();
            ProtoResponse protoResponse = ProtoResponse.newBuilder().setGetTopicsResponse(getTopicsResponse).build();
            return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
        });
    }

    private CompletionStage<GetTopicResponse> handleTopicStage(CompletionStage<Topic> topicCompletionStage, int topicId){
        return topicCompletionStage.handle((topic, throwable) -> {
            VStatus.Builder vBuilder = VStatus.newBuilder();
            GetTopicResponse.Builder topicBuilder = GetTopicResponse.newBuilder();
            if (throwable != null){
                VStatus vStatus = vBuilder.setMessage(throwable.getMessage()).setStatusCode(StatusCode.Failure).build();
                return topicBuilder.setTopic(ProtoTopic.newBuilder().setId(topicId).build()).setStatus(vStatus).build();
            } else {
                VStatus vStatus = vBuilder.setStatusCode(StatusCode.Success).build();
                try {
                    return topicBuilder.setTopic(ProtoTopic.parseFrom(topic.toBytes())).setStatus(vStatus).build();
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
