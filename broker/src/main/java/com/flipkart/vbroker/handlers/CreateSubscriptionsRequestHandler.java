package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import com.google.common.util.concurrent.ListeningExecutorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class CreateSubscriptionsRequestHandler implements RequestHandler {

    private final SubscriptionService subscriptionService;
    private final ListeningExecutorService listeningExecutorService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        CreateSubscriptionsRequest createSubscriptionsRequest = FlatbufUtils.getProtoRequest(vRequest).getCreateSubscriptionsRequest();
        List<CompletionStage<CreateSubscriptionResponse>> stages = createSubscriptionsRequest.getSubscriptionsList().stream()
            .map(Subscription::new)
            .map(subscription -> {
                log.info("Creating subscription with id {}, name {}", subscription.id(), subscription.name());
                return subscriptionService.createSubscription(subscription)
                    .thenApply(subscription1 -> {
                        VStatus vStatus = VStatus.newBuilder().setMessage("").setStatusCode(StatusCode.Success).build();
                        return CreateSubscriptionResponse.newBuilder()
                            .setName(subscription1.name())
                            .setTopicId(subscription1.topicId())
                            .setStatus(vStatus)
                            .build();
                    })
                    .exceptionally(throwable -> {
                        VStatus vStatus = VStatus.newBuilder().setMessage(throwable.getMessage()).setStatusCode(StatusCode.SubscriptionCreateFailed_Validation).build();
                        return CreateSubscriptionResponse.newBuilder()
                            .setName(subscription.name())
                            .setTopicId(subscription.topicId())
                            .setStatus(vStatus)
                            .build();
                    });
            }).collect(Collectors.toList());

        return CompletionStageUtils.listOfStagesToStageOfList(stages).thenApply(createSubscriptionResponses -> {
            CreateSubscriptionsResponse createSubscriptionsResponse = CreateSubscriptionsResponse.newBuilder()
                .addAllCreateSubscriptionResponses(createSubscriptionResponses).build();
            ProtoResponse protoResponse = ProtoResponse.newBuilder()
                .setCreateSubscriptionsResponse(createSubscriptionsResponse).build();
            return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
        });
    }
}
