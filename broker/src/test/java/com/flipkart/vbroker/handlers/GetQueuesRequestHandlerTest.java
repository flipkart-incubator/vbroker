package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.GetQueuesRequest;
import com.flipkart.vbroker.proto.GetQueuesResponse;
import com.flipkart.vbroker.proto.ProtoQueue;
import com.flipkart.vbroker.proto.ProtoRequest;
import com.flipkart.vbroker.services.QueueService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Queue;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by kaushal.hooda on 05/03/18.
 */
public class GetQueuesRequestHandlerTest {
    private GetQueuesRequestHandler getQueuesRequestHandler;
    private QueueService queueService;

    @BeforeMethod
    public void setUp() {
        queueService = Mockito.mock(QueueService.class);
        getQueuesRequestHandler = new GetQueuesRequestHandler(queueService);
    }

    @Test
    public void shouldReturnQueue() {
        Queue queue = new Queue(ProtoQueue.newBuilder().setId(1).build());
        Mockito.when(queueService.getQueue(1)).thenReturn(CompletableFuture.completedFuture(queue));

        VRequest vRequest = generateVRequest(Collections.singletonList(1));
        VResponse vResponse = getQueuesRequestHandler.handle(vRequest).toCompletableFuture().join();
        GetQueuesResponse response = FlatbufUtils.getProtoResponse(vResponse).getGetQueuesResponse();

        Assert.assertEquals(response.getQueueResponses(0).getQueue().getId(), 1);
    }

    @Test
    public void shouldHandleOneMissingOnePresentQueue() {
        Queue queue = new Queue(ProtoQueue.newBuilder().setId(1).build());
        Mockito.when(queueService.getQueue(1)).thenReturn(CompletableFuture.completedFuture(queue));
        Mockito.when(queueService.getQueue(2)).thenReturn(CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Queue not found");
        }));

        VRequest vRequest = generateVRequest(Arrays.asList(1, 2));
        VResponse vResponse = getQueuesRequestHandler.handle(vRequest).toCompletableFuture().join();

        GetQueuesResponse response = FlatbufUtils.getProtoResponse(vResponse).getGetQueuesResponse();
        Assert.assertEquals(response.getQueueResponses(0).getQueue().getId(), 1);
        Assert.assertEquals(response.getQueueResponses(1).getStatus().getStatusCode(), StatusCode.Failure);
    }

    private VRequest generateVRequest(List<Integer> queueIds) {
        GetQueuesRequest getQueuesRequest = GetQueuesRequest.newBuilder().addAllIds(queueIds).build();
        return FlatbufUtils.createVRequest((byte) 1, 1001, ProtoRequest.newBuilder().setGetQueuesRequest(getQueuesRequest).build());
    }

}