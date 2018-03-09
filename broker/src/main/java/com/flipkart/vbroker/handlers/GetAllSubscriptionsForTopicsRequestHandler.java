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

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 06/03/18.
 */
@Slf4j
@AllArgsConstructor
public class GetAllSubscriptionsForTopicsRequestHandler implements RequestHandler {
    private final SubscriptionService subscriptionService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        ProtoRequest protoRequest = FlatbufUtils.getProtoRequest(vRequest);
        assert protoRequest.hasGetAllSubscriptionsForTopicsRequest();

        GetAllSubscriptionsForTopicsRequest getAllSubsForTopicsReq = protoRequest.getGetAllSubscriptionsForTopicsRequest();
        List<CompletionStage<GetAllSubscriptionsForTopicResponse>> stages = getAllSubsForTopicsReq.getTopicIdsList().stream()
            .map(this::getSubscriptionsForTopic)
            .collect(Collectors.toList());

        return CompletionStageUtils.listOfStagesToStageOfList(stages).thenApply(subscriptionsForTopicResponses -> {
            GetAllSubscriptionsForTopicsResponse getAllSubscriptionsForTopicsResponse = GetAllSubscriptionsForTopicsResponse.newBuilder().addAllGetAllSubscriptionsForTopicResponses(subscriptionsForTopicResponses).build();
            ProtoResponse protoResponse = ProtoResponse.newBuilder().setGetAllSubscriptionsForTopicsResponse(getAllSubscriptionsForTopicsResponse).build();
            return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
        });
    }

    private CompletionStage<GetAllSubscriptionsForTopicResponse> getSubscriptionsForTopic(int topicId) {
        CompletionStage<List<Subscription>> listCompletionStage = subscriptionService.getSubscriptionsForTopic(topicId);
        return handleSubscriptionsStage(listCompletionStage, topicId);
    }

    private CompletionStage<GetAllSubscriptionsForTopicResponse> handleSubscriptionsStage(CompletionStage<List<Subscription>> subscriptionsStage, int topicId) {
        return subscriptionsStage.handle((subscriptions, throwable) -> {
            VStatus.Builder vBuilder = VStatus.newBuilder();
            VStatus status;
            GetAllSubscriptionsForTopicResponse.Builder subBuilder = GetAllSubscriptionsForTopicResponse.newBuilder();
            GetAllSubscriptionsForTopicResponse subResponse;
            if (throwable != null) {
                status = vBuilder.setMessage(throwable.getMessage()).setStatusCode(StatusCode.Failure).build();
                subResponse = subBuilder.setStatus(status).setTopicId(topicId).build();
            } else {
                status = vBuilder.setStatusCode(StatusCode.Success).build();
                List<ProtoSubscription> protoSubscriptions = subscriptions.stream()
                    .map(this::getProtoSubscription)
                    .collect(Collectors.toList());
                subResponse = subBuilder.setStatus(status).addAllSubscriptions(protoSubscriptions).setTopicId(topicId).build();
            }
            return subResponse;
        });
    }

    private ProtoSubscription getProtoSubscription(Subscription subscription) {
        try {
            return ProtoSubscription.parseFrom(subscription.toBytes());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
