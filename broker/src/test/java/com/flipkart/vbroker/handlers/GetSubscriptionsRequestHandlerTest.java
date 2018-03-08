package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.GetSubscriptionsRequest;
import com.flipkart.vbroker.proto.GetSubscriptionsResponse;
import com.flipkart.vbroker.proto.ProtoRequest;
import com.flipkart.vbroker.proto.TopicSubscription;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.google.flatbuffers.FlatBufferBuilder;
import javafx.util.Pair;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
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
        getSubscriptionsRequestHandler = new GetSubscriptionsRequestHandler();
    }

    @Test
    public void shouldReturnExistingSubscription() {
//        Mockito.when(subscriptionService.getSubscription((short) 1, (short) 1))
//            .thenReturn(DummyEntities.groupedSubscription)
        VRequest vRequest = generateVRequeset(Collections.singletonList(new Pair<>(1, 1)));
        VResponse response = getSubscriptionsRequestHandler.handle(vRequest).toCompletableFuture().join();
        GetSubscriptionsResponse subscriptionsResponse = FlatbufUtils.getProtoResponse(response).getGetSubscriptionsResponse();
    }

    private VRequest generateVRequeset(List<Pair<Integer, Integer>> topicSubscriptions) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
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