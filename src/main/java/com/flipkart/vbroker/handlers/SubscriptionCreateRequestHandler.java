package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.Subscription;
import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.entities.ResponseMessage;
import com.flipkart.vbroker.entities.SubscriptionCreateRequest;
import com.flipkart.vbroker.entities.SubscriptionCreateResponse;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.entities.VResponse;
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
		Topic topic = Topic.TopicBuilder.aTopic().withId(subscriptionCreateRequest.topicId()).build();
		Subscription subscription = Subscription.SubscriptionBuilder.aSubscription()
				.withId(subscriptionCreateRequest.subscriptionId()).withName(subscriptionCreateRequest.name())
				.withGrouped(subscriptionCreateRequest.grouped()).withTopic(topic).build();
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
