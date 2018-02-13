package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class SubscriptionCreateRequestHandler implements RequestHandler {

    private final SubscriptionService subscriptionService;
    private final ListeningExecutorService listeningExecutorService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        SubscriptionCreateRequest subscriptionCreateRequest = (SubscriptionCreateRequest) vRequest
            .requestMessage(new SubscriptionCreateRequest());
        assert nonNull(subscriptionCreateRequest);

        Subscription subscription = subscriptionCreateRequest.subscription();

        return CompletableFuture.supplyAsync(() -> {
            log.info("Creating subscription with id {}, name {}", subscription.subscriptionId(), subscription.name());
            subscriptionService.createSubscription(subscription);

            FlatBufferBuilder subscriptionResponseBuilder = new FlatBufferBuilder();
            int status = VStatus.createVStatus(subscriptionResponseBuilder, StatusCode.Success, subscriptionResponseBuilder.createString(""));
            int subscriptionCreateResponse = SubscriptionCreateResponse
                .createSubscriptionCreateResponse(subscriptionResponseBuilder, subscription.subscriptionId(), status);
            int vResponse = VResponse.createVResponse(subscriptionResponseBuilder, 1003,
                ResponseMessage.SubscriptionCreateResponse, subscriptionCreateResponse);
            subscriptionResponseBuilder.finish(vResponse);
            return VResponse.getRootAsVResponse(subscriptionResponseBuilder.dataBuffer());
        });
    }
}
