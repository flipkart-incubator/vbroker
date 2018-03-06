package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.QueueService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Queue;
import com.google.protobuf.InvalidProtocolBufferException;
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
public class GetAllQueuesRequestHandler implements RequestHandler {
    private final QueueService queueService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        ProtoRequest protoRequest = FlatbufUtils.getProtoRequest(vRequest);
        assert protoRequest.hasGetAllQueuesRequest();

        return queueService.getAllQueues().handle((queues, throwable) -> {
            VStatus.Builder vBuilder = VStatus.newBuilder();
            GetAllQueuesResponse.Builder qBuilder = GetAllQueuesResponse.newBuilder();
            VStatus status;
            GetAllQueuesResponse getAllQueuesResponse;

            if (throwable != null) {
                status = vBuilder.setMessage(throwable.getMessage()).setStatusCode(StatusCode.Failure).build();
                getAllQueuesResponse = qBuilder.setStatus(status).build();
            } else {
                List<ProtoQueue> protoQueues = queues.stream().map(this::getProtoQueue).collect(Collectors.toList());
                status = vBuilder.setStatusCode(StatusCode.Success).build();
                getAllQueuesResponse = qBuilder.setStatus(status).addAllQueues(protoQueues).build();
            }

            ProtoResponse protoResponse = ProtoResponse.newBuilder().setGetAllQueuesResponse(getAllQueuesResponse).build();
            return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
        });
    }

    private ProtoQueue getProtoQueue(Queue queue) {
        try {
            return ProtoQueue.parseFrom(queue.toBytes());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
