package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 06/03/18.
 */

@Slf4j
@AllArgsConstructor
public class GetAllTopicsRequestHandler implements RequestHandler {
    private final TopicService topicService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        ProtoRequest protoRequest = FlatbufUtils.getProtoRequest(vRequest);
        assert protoRequest.hasGetAllTopicsRequest();

        return topicService.getAllTopics().handle((topics, throwable) -> {
            VStatus.Builder vBuilder = VStatus.newBuilder();
            GetAllTopicsResponse.Builder topicBuilder = GetAllTopicsResponse.newBuilder();
            VStatus status;
            GetAllTopicsResponse getAllTopicsResponse;

            if(throwable != null){
                status = vBuilder.setMessage(throwable.getMessage()).setStatusCode(StatusCode.Failure).build();
                getAllTopicsResponse = topicBuilder.setStatus(status).build();
            } else {
                List<ProtoTopic> protoTopics = topics.stream().map(this::getProtoTopic).collect(Collectors.toList());
                status = vBuilder.setStatusCode(StatusCode.Success).build();
                getAllTopicsResponse = topicBuilder.setStatus(status).addAllTopics(protoTopics).build();
            }

            ProtoResponse protoResponse = ProtoResponse.newBuilder().setGetAllTopicsResponse(getAllTopicsResponse).build();
            return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
        });
    }

    private ProtoTopic getProtoTopic(Topic topic) {
        try {
            return ProtoTopic.parseFrom(topic.toBytes());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
