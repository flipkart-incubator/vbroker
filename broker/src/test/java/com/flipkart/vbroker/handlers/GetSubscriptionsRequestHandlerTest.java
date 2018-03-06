package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import javafx.util.Pair;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 27/02/18.
 */

public class GetSubscriptionsRequestHandlerTest {

    private GetSubscriptionsRequestHandler getSubscriptionsRequestHandler;
    private SubscriptionService subscriptionService;

    @BeforeMethod
    public void setUp() throws Exception {
        subscriptionService = Mockito.mock(SubscriptionService.class);
        getSubscriptionsRequestHandler = new GetSubscriptionsRequestHandler(subscriptionService);
    }

    @Test
    public void shouldReturnExistingSubscription() {
        Subscription subscription = new Subscription(ProtoSubscription.newBuilder().setId(1).setName("subscription_1").build());
        Mockito.when(subscriptionService.getSubscription(1, 1))
            .thenReturn(CompletableFuture.completedFuture(subscription));

        VRequest vRequest = generateVRequest(Collections.singletonList(new Pair<>(1, 1)));
        VResponse response = getSubscriptionsRequestHandler.handle(vRequest).toCompletableFuture().join();
        GetSubscriptionsResponse subscriptionsResponse = FlatbufUtils.getProtoResponse(response).getGetSubscriptionsResponse();

        Assert.assertEquals("subscription_1", subscriptionsResponse.getSubscriptionResponses(0).getSubscription().getName());
        Assert.assertEquals(subscription.toBytes(), subscriptionsResponse.getSubscriptionResponses(0).getSubscription().toByteArray());
    }

    @Test
    public void shouldHandleOneMissingOneExistingSubscription(){
        Subscription subscription = new Subscription(ProtoSubscription.newBuilder().setId(1).setName("subscription_1").build());
        Mockito.when(subscriptionService.getSubscription(1, 1))
            .thenReturn(CompletableFuture.completedFuture(subscription));
        Mockito.when(subscriptionService.getSubscription(2, 2))
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Subscription not found");
            }));


        VRequest vRequest = generateVRequest(Arrays.asList(new Pair<>(1,1), new Pair<>(2,2)));
        VResponse response = getSubscriptionsRequestHandler.handle(vRequest).toCompletableFuture().join();
        GetSubscriptionsResponse subscriptionsResponse = FlatbufUtils.getProtoResponse(response).getGetSubscriptionsResponse();
        List<GetSubscriptionResponse> subscriptionResponses = subscriptionsResponse.getSubscriptionResponsesList();

        Assert.assertEquals("subscription_1", subscriptionResponses.get(0).getSubscription().getName());
        Assert.assertEquals(subscription.toBytes(), subscriptionResponses.get(0).getSubscription().toByteArray());

        Assert.assertEquals(StatusCode.Failure, subscriptionResponses.get(1).getStatus().getStatusCode());
        Assert.assertEquals(subscriptionResponses.get(1).getSubscription().getId(), 2);
        Assert.assertEquals(subscriptionResponses.get(1).getSubscription().getTopicId(), 2);
    }

    private VRequest generateVRequest(List<Pair<Integer, Integer>> topicSubscriptions) {
        List<TopicSubscription> topicSubscriptionList = topicSubscriptions.stream()
            .map(pair ->
                TopicSubscription.newBuilder()
                    .setTopicId(pair.getKey())
                    .setSubscriptionId(pair.getValue())
                    .build())
            .collect(Collectors.toList());

        GetSubscriptionsRequest getSubscriptionsRequest = GetSubscriptionsRequest
            .newBuilder()
            .addAllSubscriptions(topicSubscriptionList)
            .build();
        ProtoRequest protoRequest = ProtoRequest.newBuilder().setGetSubscriptionsRequest(getSubscriptionsRequest).build();
        return FlatbufUtils.createVRequest((byte) 1, 1001, protoRequest);
    }
}