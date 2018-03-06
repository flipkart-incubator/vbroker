package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.QueueService;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Queue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 06/03/18.
 */
@AllArgsConstructor
@Slf4j
public class CreateQueuesRequestHandler implements RequestHandler {
    private final QueueService queueService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        CreateQueuesRequest createQueuesRequest = FlatbufUtils.getProtoRequest(vRequest).getCreateQueuesRequest();
        List<CompletionStage<CreateQueueResponse>> stages = createQueuesRequest.getQueuesList().stream()
            .map(Queue::new)
            .map(this::createQueue)
            .collect(Collectors.toList());


        return CompletionStageUtils.listOfStagesToStageOfList(stages).thenApply(createQueueResponses -> {
            CreateQueuesResponse createQueuesResponse = CreateQueuesResponse
                .newBuilder()
                .addAllCreateQueueResponses(createQueueResponses)
                .build();
            ProtoResponse protoResponse = ProtoResponse.newBuilder().setCreateQueuesResponse(createQueuesResponse).build();
            return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
        });
    }

    private CompletionStage<CreateQueueResponse> createQueue(Queue queue) {
        return queueService.createQueue(queue).handle((ignored, throwable) -> {
            int statusCode = StatusCode.Success;
            String message = "";
            if (throwable != null) {
                statusCode = StatusCode.Failure;
                message = throwable.getMessage();
            }
            VStatus status = VStatus.newBuilder().setStatusCode(statusCode).setMessage(message).build();
            return CreateQueueResponse.newBuilder().setQueueName(queue.topic().name()).setStatus(status).build();
        });
    }
}
