package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 27/02/18.
 */
@Slf4j
@AllArgsConstructor
public class GetSubscriptionsRequestHandler implements RequestHandler {
    private final SubscriptionService subscriptionService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        GetSubscriptionsRequest request = FlatbufUtils.getProtoRequest(vRequest).getGetSubscriptionsRequest();
        List<CompletionStage<GetSubscriptionResponse>> stages = new ArrayList<>();
        for (TopicSubscription topicSub : request.getSubscriptionsList()) {
            CompletionStage<Subscription> subscriptionStage = subscriptionService.getSubscription(topicSub.getTopicId(), topicSub.getSubscriptionId());
            stages.add(handleSubscriptionStage(subscriptionStage, topicSub));
        }
        return CompletionStageUtils.listOfStagesToStageOfList(stages)
            .thenApply(getSubscriptionResponses -> generateVResponse(vRequest, getSubscriptionResponses));
    }

    private VResponse generateVResponse(VRequest vRequest, List<GetSubscriptionResponse> getSubscriptionResponses) {
        GetSubscriptionsResponse getSubscriptionsResponse = GetSubscriptionsResponse.newBuilder().addAllSubscriptions(getSubscriptionResponses).build();
        ProtoResponse protoResponse = ProtoResponse.newBuilder().setGetSubscriptionsResponse(getSubscriptionsResponse).build();
        return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
    }

    private CompletionStage<GetSubscriptionResponse> handleSubscriptionStage(CompletionStage<Subscription> subscriptionCompletionStage, TopicSubscription requested){
        return subscriptionCompletionStage.handle((subscription, throwable) -> {
            VStatus.Builder vBuilder = VStatus.newBuilder();
            GetSubscriptionResponse.Builder subBuilder = GetSubscriptionResponse.newBuilder();
            if (throwable != null){
                ProtoSubscription protoSubscription = ProtoSubscription.newBuilder().setId(requested.getSubscriptionId()).setTopicId(requested.getTopicId()).build();
                VStatus vStatus = vBuilder.setStatusCode(StatusCode.Failure).setMessage(throwable.getMessage()).build();
                return subBuilder.setStatus(vStatus).setSubscription(protoSubscription).build();
            } else {
                VStatus vStatus = vBuilder.setStatusCode(StatusCode.Success).build();
                try {
                    return subBuilder.setStatus(vStatus).setSubscription(ProtoSubscription.parseFrom(subscription.toBytes())).build();
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
