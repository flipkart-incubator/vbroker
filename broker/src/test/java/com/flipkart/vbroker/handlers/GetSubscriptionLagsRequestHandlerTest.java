package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by kaushal.hooda on 19/02/18.
 */
@Slf4j
public class GetSubscriptionLagsRequestHandlerTest {
    private VRequest vRequest;
    private SubscriptionService subscriptionService;
    private GetSubscriptionLagsRequestHandler getSubscriptionLagsRequestHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        subscriptionService = mock(SubscriptionService.class);
        getSubscriptionLagsRequestHandler = new GetSubscriptionLagsRequestHandler(subscriptionService);
    }

    @Test
    public void shouldGetLagForOneSubscriptionWithOnePartition() {
        vRequest = generateVRequest(lagsReqForOnePartition());

        Subscription subscription = mock(Subscription.class);
        PartSubscription partSubscription = mock(PartSubscription.class);

        when(subscriptionService.getSubscription(1, 1))
            .thenReturn(CompletableFuture.completedFuture(subscription));
        when(subscriptionService.getPartSubscription(subscription, 1))
            .thenReturn(CompletableFuture.completedFuture(partSubscription));
        when(subscriptionService.getPartSubscriptionLag(partSubscription))
            .thenReturn(CompletableFuture.completedFuture(5));
        when(partSubscription.getId())
            .thenReturn(1);

        CompletionStage<VResponse> responseCompletionStage = getSubscriptionLagsRequestHandler.handle(vRequest);
        VResponse response = responseCompletionStage.toCompletableFuture().join();
        GetSubscriptionLagsResponse getSubscriptionLagsResponse = FlatbufUtils.getProtoResponse(response).getGetSubscriptionLagsResponse();
        int lag = getSubscriptionLagsResponse.getSubscriptionLags(0).getPartitionLags(0).getLag();
        Assert.assertEquals(lag, 5);
    }

    @Test
    public void shouldCatchExceptionInGetPartSubscriptionLag() {
        vRequest = generateVRequest(lagsReqForOnePartition());

        Subscription subscription = mock(Subscription.class);
        PartSubscription partSubscription = mock(PartSubscription.class);

        when(subscriptionService.getSubscription(1, 1))
            .thenReturn(CompletableFuture.completedFuture(subscription));
        when(subscriptionService.getPartSubscription(subscription, 1))
            .thenReturn(CompletableFuture.completedFuture(partSubscription));
        when(subscriptionService.getPartSubscriptionLag(partSubscription))
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Error in getting lag");
            }));
        when(partSubscription.getId())
            .thenReturn(1);

        CompletionStage<VResponse> responseCompletionStage = getSubscriptionLagsRequestHandler.handle(vRequest);
        VResponse response = responseCompletionStage.toCompletableFuture().join();
        GetSubscriptionLagsResponse getSubscriptionLagsResponse = FlatbufUtils.getProtoResponse(response).getGetSubscriptionLagsResponse();
        int lag = getSubscriptionLagsResponse.getSubscriptionLags(0).getPartitionLags(0).getLag();
        Assert.assertEquals(lag, -1);
    }

    @Test
    public void shouldCatchExceptionInGetPartSubscription() {
        vRequest = generateVRequest(lagsReqForOnePartition());

        Subscription subscription = mock(Subscription.class);
        when(subscriptionService.getSubscription(1, 1))
            .thenReturn(CompletableFuture.completedFuture(subscription));
        when(subscriptionService.getPartSubscription(subscription, 1))
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Error in getting PartSubscription");
            }));

        CompletionStage<VResponse> responseCompletionStage = getSubscriptionLagsRequestHandler.handle(vRequest);
        VResponse response = responseCompletionStage.toCompletableFuture().join();
        GetSubscriptionLagsResponse getSubscriptionLagsResponse = FlatbufUtils.getProtoResponse(response).getGetSubscriptionLagsResponse();
        int lag = getSubscriptionLagsResponse.getSubscriptionLags(0).getPartitionLags(0).getLag();
        Assert.assertEquals(lag, -1);
    }

    @Test
    void shouldCatchExceptionInGetSubscription() {
        vRequest = generateVRequest(lagsReqForOnePartition());

        when(subscriptionService.getSubscription(1, 1))
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Error in getting Subscription");
            }));
        CompletionStage<VResponse> responseCompletionStage = getSubscriptionLagsRequestHandler.handle(vRequest);
        VResponse response = responseCompletionStage.toCompletableFuture().join();
        GetSubscriptionLagsResponse getSubscriptionLagsResponse = FlatbufUtils.getProtoResponse(response).getGetSubscriptionLagsResponse();
        int lag = getSubscriptionLagsResponse.getSubscriptionLags(0).getPartitionLags(0).getLag();
        log.info(getSubscriptionLagsResponse.getSubscriptionLags(0).getPartitionLags(0).getStatus().getMessage());
        Assert.assertEquals(lag, -1);
    }


    private GetSubscriptionLagsRequest lagsReqForOnePartition() {
        TopicSubscription topicSubscription = TopicSubscription.newBuilder().setTopicId(1).setSubscriptionId(1).build();
        PartitionLagRequest partitionLagRequest = PartitionLagRequest.newBuilder().setPartitionId(1).build();
        SubscriptionLagRequest subscriptionLagRequest = SubscriptionLagRequest.newBuilder()
            .setTopicSubscription(topicSubscription)
            .addPartitionLags(partitionLagRequest).build();
        return GetSubscriptionLagsRequest.newBuilder().addSubscriptionLagRequests(subscriptionLagRequest).build();
    }

    private VRequest generateVRequest(GetSubscriptionLagsRequest getSubscriptionLagsRequest) {
        ProtoRequest protoRequest = ProtoRequest.newBuilder().setGetSubscriptionLagsRequest(getSubscriptionLagsRequest).build();
        return FlatbufUtils.createVRequest((byte) 1, 1001, protoRequest);
    }

    private void setUpSubscriptionServiceMocks() {

    }
}