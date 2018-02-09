package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.Subscription;
import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SubscriptionCreateRequestHandler implements RequestHandler {

    private final SubscriptionService subscriptionService;

    @Override
    public VResponse handle(VRequest vRequest) {

        SubscriptionCreateRequest subscriptionCreateRequest = (SubscriptionCreateRequest) vRequest
                .requestMessage(new SubscriptionCreateRequest());
        Topic topic = Topic.TopicBuilder.aTopic().withId(subscriptionCreateRequest.subscription().topicId()).build();
        Subscription subscription = Subscription.SubscriptionBuilder.aSubscription()
                .withId(subscriptionCreateRequest.subscription().subscriptionId()).withName(subscriptionCreateRequest.subscription().name())
                .withGrouped(subscriptionCreateRequest.subscription().grouped()).withTopic(topic).build();
        log.info("Creating subscription with id {}, name {}", subscription.getId(), subscription.getName());
        subscriptionService.createSubscription(subscription);

        FlatBufferBuilder subscriptionResponseBuilder = new FlatBufferBuilder();
        int subscriptionCreateResponse = SubscriptionCreateResponse
                .createSubscriptionCreateResponse(subscriptionResponseBuilder, subscription.getId(), (short) 200);
        int vresponse = VResponse.createVResponse(subscriptionResponseBuilder, 1003,
                ResponseMessage.SubscriptionCreateResponse, subscriptionCreateResponse);
        subscriptionResponseBuilder.finish(vresponse);
        return VResponse.getRootAsVResponse(subscriptionResponseBuilder.dataBuffer());
    }

}
