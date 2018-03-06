package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.QueueService;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Queue;
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
public class GetQueuesRequestHandler implements RequestHandler {
    private final QueueService queueService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        GetQueuesRequest getQueuesRequest = FlatbufUtils.getProtoRequest(vRequest).getGetQueuesRequest();

        List<CompletionStage<GetQueueResponse>> stages = new ArrayList<>();
        for (Integer queueId : getQueuesRequest.getIdsList()) {
            CompletionStage<Queue> queueCompletionStage = queueService.getQueue(queueId);
            CompletionStage<GetQueueResponse> getQueueResponseStage = handleQueueStage(queueCompletionStage, queueId);
            stages.add(getQueueResponseStage);
        }

        return CompletionStageUtils.listOfStagesToStageOfList(stages).thenApply(getTopicResponses -> {
            GetQueuesResponse getQueuesResponse = GetQueuesResponse.newBuilder().addAllQueueResponses(getTopicResponses).build();
            ProtoResponse protoResponse = ProtoResponse.newBuilder().setGetQueuesResponse(getQueuesResponse).build();
            return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
        });
    }

    private CompletionStage<GetQueueResponse> handleQueueStage(CompletionStage<Queue> queueCompletionStage, int queueId) {
        return queueCompletionStage.handle((queue, throwable) -> {
            VStatus.Builder vBuilder = VStatus.newBuilder();
            GetQueueResponse.Builder queueBuilder = GetQueueResponse.newBuilder();
            if (throwable != null) {
                VStatus vStatus = vBuilder.setMessage(throwable.getMessage()).setStatusCode(StatusCode.Failure).build();
                return queueBuilder.setQueue(ProtoQueue.newBuilder().setId(queueId).build()).setStatus(vStatus).build();
            } else {
                VStatus vStatus = vBuilder.setStatusCode(StatusCode.Success).build();
                try {
                    return queueBuilder.setQueue(ProtoQueue.parseFrom(queue.toBytes())).setStatus(vStatus).build();
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
