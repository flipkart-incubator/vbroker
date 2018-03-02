package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.google.flatbuffers.FlatBufferBuilder;
import javafx.util.Pair;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

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
    public void shouldReturnExistingSubscription(){
//        Mockito.when(subscriptionService.getSubscription((short) 1, (short) 1))
//            .thenReturn(DummyEntities.groupedSubscription)
        VRequest vRequest = generateVRequeset(Arrays.asList(new Pair<>((short) 1, (short) 1)));
        VResponse response = getSubscriptionsRequestHandler.handle(vRequest).toCompletableFuture().join();
        GetSubscriptionsResponse subscriptionsResponse = (GetSubscriptionsResponse) response.responseMessage(new GetSubscriptionsResponse());
    }

    private VRequest generateVRequeset(List<Pair<Short, Short>> topicSubscriptions){
        FlatBufferBuilder builder = new FlatBufferBuilder();
        List<TopicSubscription> subscriptions = topicSubscriptions.stream()
            .map(pair -> FlatbufUtils.createTopicSubscription(pair.getKey(), pair.getValue())).collect(Collectors.toList());
        int getSubscriptionsRequestOffset = FlatbufUtils.buildGetSubscriptionsRequest(builder, subscriptions);
        int vRequest = VRequest.createVRequest(builder,
            (byte) 1,
            1001,
            RequestMessage.GetSubscriptionsRequest,
            getSubscriptionsRequestOffset);
        builder.finish(vRequest);
        return VRequest.getRootAsVRequest(builder.dataBuffer());
    }
}