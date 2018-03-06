package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.exceptions.TopicNotFoundException;
import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * Created by kaushal.hooda on 06/03/18.
 */
public class GetAllSubscriptionsForTopicsRequestHandlerTest {
    private SubscriptionService subscriptionService;
    private GetAllSubscriptionsForTopicsRequestHandler getAllSubscriptionsForTopicsRequestHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        subscriptionService = mock(SubscriptionService.class);
        getAllSubscriptionsForTopicsRequestHandler = new GetAllSubscriptionsForTopicsRequestHandler(subscriptionService);
    }

    @Test
    public void shouldHandlePartialSuccess(){
        Subscription subscription = new Subscription(ProtoSubscription.newBuilder().setId(1).setName("subscription_1").build());
        when(subscriptionService.getSubscriptionsForTopic(1))
            .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(subscription)));
        when(subscriptionService.getSubscriptionsForTopic(2))
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new TopicNotFoundException("Topic not found");
            }));

        VRequest vRequest = generateVRequest(Arrays.asList(1,2));
        VResponse vResponse = getAllSubscriptionsForTopicsRequestHandler.handle(vRequest).toCompletableFuture().join();
        List<GetAllSubscriptionsForTopicResponse> responses = FlatbufUtils.getProtoResponse(vResponse).getGetAllSubscriptionsForTopicsResponse().getGetAllSubscriptionsForTopicResponsesList();

        Assert.assertEquals(responses.get(0).getTopicId(), 1);
        Assert.assertEquals(responses.get(0).getSubscriptionsCount(), 1);
        Assert.assertEquals(responses.get(0).getSubscriptions(0).toByteArray(), subscription.toBytes());

        Assert.assertEquals(responses.get(1).getTopicId(), 2);
        Assert.assertEquals(responses.get(1).getSubscriptionsCount(), 0);
        Assert.assertEquals(responses.get(1).getStatus().getStatusCode(), StatusCode.Failure);

    }


    private VRequest generateVRequest(List<Integer> topics) {
        GetAllSubscriptionsForTopicsRequest getAllSubscriptionsForTopicsRequest = GetAllSubscriptionsForTopicsRequest.newBuilder().addAllTopicIds(topics).build();
        ProtoRequest protoRequest = ProtoRequest.newBuilder().setGetAllSubscriptionsForTopicsRequest(getAllSubscriptionsForTopicsRequest).build();
        return FlatbufUtils.createVRequest((byte) 1, 1001, protoRequest);
    }


}