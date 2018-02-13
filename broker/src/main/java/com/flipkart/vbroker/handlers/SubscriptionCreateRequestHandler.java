package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class SubscriptionCreateRequestHandler implements RequestHandler {

    private final SubscriptionService subscriptionService;
    private final ListeningExecutorService listeningExecutorService;

    @Override
    public ListenableFuture<VResponse> handle(VRequest vRequest) {
        SubscriptionCreateRequest subscriptionCreateRequest = (SubscriptionCreateRequest) vRequest
            .requestMessage(new SubscriptionCreateRequest());
        assert nonNull(subscriptionCreateRequest);

        return listeningExecutorService.submit(() -> {
            log.info("Creating subscription with id {}, name {}", subscriptionCreateRequest.subscription().subscriptionId(), subscriptionCreateRequest.subscription().name());
            subscriptionService.createSubscription(subscriptionCreateRequest.subscription());

            FlatBufferBuilder subscriptionResponseBuilder = new FlatBufferBuilder();
            int status = VStatus.createVStatus(subscriptionResponseBuilder, StatusCode.Success, subscriptionResponseBuilder.createString(""));
            int subscriptionCreateResponse = SubscriptionCreateResponse
                .createSubscriptionCreateResponse(subscriptionResponseBuilder, subscriptionCreateRequest.subscription().subscriptionId(), status);
            int vresponse = VResponse.createVResponse(subscriptionResponseBuilder, 1003,
                ResponseMessage.SubscriptionCreateResponse, subscriptionCreateResponse);
            subscriptionResponseBuilder.finish(vresponse);
            return VResponse.getRootAsVResponse(subscriptionResponseBuilder.dataBuffer());
        });
    }

}
