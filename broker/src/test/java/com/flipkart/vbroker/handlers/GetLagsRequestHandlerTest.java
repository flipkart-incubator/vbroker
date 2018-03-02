package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.RequestMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.entities.VResponse;
import com.flipkart.vbroker.services.SubscriptionService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by kaushal.hooda on 19/02/18.
 */
@Slf4j
public class GetLagsRequestHandlerTest {
    private VRequest vRequest;
    private SubscriptionService subscriptionService;
    private GetLagsRequestHandler getLagsRequestHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        subscriptionService = mock(SubscriptionService.class);
        getLagsRequestHandler = new GetLagsRequestHandler(subscriptionService);
    }

    @Test
    public void shouldGetLagForOneSubscriptionWithOnePartition() {
        vRequest = generateVRequest(lagsReqForOnePartition());

        Subscription subscription = mock(Subscription.class);
        PartSubscription partSubscription = mock(PartSubscription.class);

        when(subscriptionService.getSubscription((short) 1, (short) 1))
            .thenReturn(CompletableFuture.completedFuture(subscription));
        when(subscriptionService.getPartSubscription(subscription, (short) 1))
            .thenReturn(CompletableFuture.completedFuture(partSubscription));
        when(subscriptionService.getPartSubscriptionLag(partSubscription))
            .thenReturn(CompletableFuture.completedFuture(5));
        when(partSubscription.getId())
            .thenReturn((short) 1);

        CompletionStage<VResponse> responseCompletionStage = getLagsRequestHandler.handle(vRequest);
        VResponse response = responseCompletionStage.toCompletableFuture().join();
        GetLagsResponse getLagsResponse = (GetLagsResponse) response.responseMessage(new GetLagsResponse());
        int lag = getLagsResponse.subscriptionLags(0).partitionLags(0).lag();
        Assert.assertEquals(lag, 5);
    }

    @Test
    public void shouldCatchExceptionInGetPartSubscriptionLag() {
        vRequest = generateVRequest(lagsReqForOnePartition());

        Subscription subscription = mock(Subscription.class);
        PartSubscription partSubscription = mock(PartSubscription.class);

        when(subscriptionService.getSubscription((short) 1, (short) 1))
            .thenReturn(CompletableFuture.completedFuture(subscription));
        when(subscriptionService.getPartSubscription(subscription, (short) 1))
            .thenReturn(CompletableFuture.completedFuture(partSubscription));
        when(subscriptionService.getPartSubscriptionLag(partSubscription))
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Error in getting lag");
            }));
        when(partSubscription.getId())
            .thenReturn((short) 1);

        CompletionStage<VResponse> responseCompletionStage = getLagsRequestHandler.handle(vRequest);
        VResponse response = responseCompletionStage.toCompletableFuture().join();
        GetLagsResponse getLagsResponse = (GetLagsResponse) response.responseMessage(new GetLagsResponse());
        int lag = getLagsResponse.subscriptionLags(0).partitionLags(0).lag();
        Assert.assertEquals(lag, -1);
    }

    @Test
    public void shouldCatchExceptionInGetPartSubscription() {
        vRequest = generateVRequest(lagsReqForOnePartition());

        Subscription subscription = mock(Subscription.class);
        when(subscriptionService.getSubscription((short) 1, (short) 1))
            .thenReturn(CompletableFuture.completedFuture(subscription));
        when(subscriptionService.getPartSubscription(subscription, (short) 1))
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Error in getting PartSubscription");
            }));

        CompletionStage<VResponse> responseCompletionStage = getLagsRequestHandler.handle(vRequest);
        VResponse response = responseCompletionStage.toCompletableFuture().join();
        GetLagsResponse getLagsResponse = (GetLagsResponse) response.responseMessage(new GetLagsResponse());
        int lag = getLagsResponse.subscriptionLags(0).partitionLags(0).lag();
        Assert.assertEquals(lag, -1);
    }

    @Test
    void shouldCatchExceptionInGetSubscription() {
        vRequest = generateVRequest(lagsReqForOnePartition());

        when(subscriptionService.getSubscription((short) 1, (short) 1))
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Error in getting Subscription");
            }));
        CompletionStage<VResponse> responseCompletionStage = getLagsRequestHandler.handle(vRequest);
        VResponse response = responseCompletionStage.toCompletableFuture().join();
        GetLagsResponse getLagsResponse = (GetLagsResponse) response.responseMessage(new GetLagsResponse());
        int lag = getLagsResponse.subscriptionLags(0).partitionLags(0).lag();
        log.info(getLagsResponse.subscriptionLags(0).partitionLags(0).status().message());
        Assert.assertEquals(lag, -1);
    }


    private DummyGetLagsReq lagsReqForOnePartition() {
        DummyGetLagsReq dummyGetLagsReq = new DummyGetLagsReq();
        dummyGetLagsReq.subscriptionLags.add(new DummyTopicSubLagReq((short) 1, (short) 1));
        dummyGetLagsReq.subscriptionLags.get(0).partitionLags.add(new DummyPartitionLagReq((short) 1));
        return dummyGetLagsReq;
    }

    private VRequest generateVRequest(DummyGetLagsReq dummyRequest) {
        List<DummyTopicSubLagReq> dummyTopicSubLagReqs = dummyRequest.subscriptionLags;
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int getLagsRequest = GetLagsRequest.createGetLagsRequest(builder, buildSubLagReqsVector(builder, dummyTopicSubLagReqs));
        int vRequest = VRequest.createVRequest(builder,
            (byte) 1,
            1001,
            RequestMessage.GetLagsRequest,
            getLagsRequest);
        builder.finish(vRequest);
        return VRequest.getRootAsVRequest(builder.dataBuffer());
    }

    private int buildSubLagReqsVector(FlatBufferBuilder builder, List<DummyTopicSubLagReq> dummyTopicSubLagReqs) {
        int[] subscriptionLagRequests = new int[dummyTopicSubLagReqs.size()];
        for (int i = 0; i < dummyTopicSubLagReqs.size(); i++) {
            DummyTopicSubLagReq dummyTopicSubLagReq = dummyTopicSubLagReqs.get(i);
            int partitionRequestsVector = buildPartitionRequestsVector(builder, dummyTopicSubLagReq.partitionLags);
            int topicProduceRequest = TopicSubscriptionLagRequest.createTopicSubscriptionLagRequest(builder,
                dummyTopicSubLagReq.subscriptionId,
                dummyTopicSubLagReq.topicId, partitionRequestsVector);
            subscriptionLagRequests[i] = topicProduceRequest;
        }
        return GetLagsRequest.createSubscriptionLagsVector(builder, subscriptionLagRequests);
    }

    private int buildPartitionRequestsVector(FlatBufferBuilder builder, List<DummyPartitionLagReq> dummyPartitionLagRequests) {
        int[] partitionLagRequests = new int[dummyPartitionLagRequests.size()];
        for (int i = 0; i < dummyPartitionLagRequests.size(); i++) {
            partitionLagRequests[i] = PartitionLagRequest.createPartitionLagRequest(builder, dummyPartitionLagRequests.get(i).partitionId);
        }
        return TopicSubscriptionLagRequest.createPartitionLagsVector(builder, partitionLagRequests);
    }

    private void setUpSubscriptionServiceMocks() {

    }

    private class DummyGetLagsReq {
        List<DummyTopicSubLagReq> subscriptionLags = new ArrayList<>();
    }

    private class DummyTopicSubLagReq {
        short topicId;
        short subscriptionId;
        List<DummyPartitionLagReq> partitionLags = new ArrayList<>();

        public DummyTopicSubLagReq(short topicId, short subscriptionId) {
            this.topicId = topicId;
            this.subscriptionId = subscriptionId;
        }
    }

    private class DummyPartitionLagReq {
        short partitionId;

        public DummyPartitionLagReq(short partitionId) {
            this.partitionId = partitionId;
        }
    }

}