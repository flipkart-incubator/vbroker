package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Queue;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 12/03/18.
 */
@AllArgsConstructor
@Slf4j
public class QueueClient {
    private final Metadata metadata;
    private final NetworkClient networkClient;

    public CompletionStage<List<CreateQueueResponse>> createQueues(List<Queue> queues) {
        List<ProtoQueue> protoQueues = queues.stream()
            .map(queue -> {
                try {
                    return ProtoQueue.parseFrom(queue.toBytes());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());

        CreateQueuesRequest createQueuesRequest = CreateQueuesRequest.newBuilder()
            .addAllQueues(protoQueues)
            .build();

        ProtoRequest protoRequest = ProtoRequest.newBuilder()
            .setCreateQueuesRequest(createQueuesRequest)
            .build();

        VRequest vRequest = FlatbufUtils.createVRequest(protoRequest);

        return networkClient.send(getNode(), vRequest).thenApply(response -> FlatbufUtils
            .getProtoResponse(response)
            .getCreateQueuesResponse()
            .getCreateQueueResponsesList());

    }

    public CompletionStage<List<GetQueueResponse>> getQueues(List<Integer> queueIds) {
        GetQueuesRequest getQueuesRequest = GetQueuesRequest.newBuilder()
            .addAllIds(queueIds)
            .build();

        ProtoRequest protoRequest = ProtoRequest.newBuilder()
            .setGetQueuesRequest(getQueuesRequest)
            .build();

        VRequest vRequest = FlatbufUtils.createVRequest(protoRequest);

        return networkClient.send(getNode(), vRequest).thenApply(response -> {
            ProtoResponse protoResponse = FlatbufUtils.getProtoResponse(response);
            assert protoResponse.hasGetQueuesResponse();
            return protoResponse.getGetQueuesResponse().getQueueResponsesList();
        });
    }

    private Node getNode() {
        return metadata.getClusterNodes().get(0);
    }
}
