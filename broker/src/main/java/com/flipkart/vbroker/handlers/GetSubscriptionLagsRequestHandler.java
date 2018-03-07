package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 19/02/18.
 */
@Slf4j
@AllArgsConstructor
public class GetSubscriptionLagsRequestHandler implements RequestHandler {
    private final SubscriptionService subscriptionService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        //TODO stopped subscriptions?
        //TODO messageGroups with no corresponding subscriberGroups? (e.g., in L3?)


        GetSubscriptionLagsRequest getSubscriptionLagsRequest = FlatbufUtils
            .getProtoRequest(vRequest)
            .getGetSubscriptionLagsRequest();
        List<CompletionStage<SubscriptionLag>> subscriptionLagStages =
            getSubscriptionLagsRequest.getSubscriptionLagRequestsList().stream()
                .map(subscriptionLagReq -> {
                    int subscriptionId = subscriptionLagReq.getTopicSubscription().getSubscriptionId();
                    int topicId = subscriptionLagReq.getTopicSubscription().getTopicId();
                    List<Integer> partitionIds = getPartitionIds(subscriptionLagReq);
                    return getSubscriptionLag(subscriptionId, topicId, partitionIds);
                })
                .collect(Collectors.toList());
        return CompletionStageUtils.listOfStagesToStageOfList(subscriptionLagStages)
            .thenApply(subscriptionLags -> generateVResponse(subscriptionLags, vRequest.correlationId()));
    }

    private CompletionStage<SubscriptionLag> getSubscriptionLag(int subscriptionId,
                                                                int topicId,
                                                                List<Integer> partitionIds) {
        //Get the subscription
        CompletionStage<Subscription> subscriptionStage = subscriptionService.getSubscription(topicId, subscriptionId);

        List<CompletionStage<PartitionLag>> partitionLagStages = partitionIds.stream()
            .map(partitionId -> getPartSubscriberLag(subscriptionStage, partitionId))
            .collect(Collectors.toList());
        return CompletionStageUtils.listOfStagesToStageOfList(partitionLagStages)
            .thenApply(lagWithPartitions -> SubscriptionLag.newBuilder()
                .setTopicId(topicId)
                .setSubscriptionId(subscriptionId)
                .addAllPartitionLags(lagWithPartitions)
                .build());
    }

    private CompletionStage<PartitionLag> getPartSubscriberLag(CompletionStage<Subscription> subscriptionStage, int partitionId) {
        return subscriptionStage
            .thenCompose(subscription -> subscriptionService.getPartSubscription(subscription, partitionId))
            .thenCompose(subscriptionService::getPartSubscriptionLag)
            .thenApply(lag -> {
                VStatus vStatus = VStatus.newBuilder()
                    .setStatusCode(StatusCode.Success)
                    .setMessage("").build();
                return PartitionLag.newBuilder()
                    .setPartitionId(partitionId)
                    .setLag(lag)
                    .setStatus(vStatus)
                    .build();
            })
            .exceptionally(throwable -> {
                log.debug("Catching an exception");
                VStatus vStatus = VStatus.newBuilder().setStatusCode(StatusCode.GetLagFailed).setMessage(throwable.getMessage()).build();
                return PartitionLag.newBuilder().setPartitionId(partitionId).setLag(-1).setStatus(vStatus).build();
            });
    }

    private VResponse generateVResponse(List<SubscriptionLag> subscriptionLags, int correlationId) {
        GetSubscriptionLagsResponse getSubscriptionLagsResponse = GetSubscriptionLagsResponse.newBuilder().addAllSubscriptionLags(subscriptionLags).build();
        ProtoResponse protoResponse = ProtoResponse.newBuilder().setGetSubscriptionLagsResponse(getSubscriptionLagsResponse).build();
        return FlatbufUtils.createVResponse(correlationId, protoResponse);
    }

    private List<Integer> getPartitionIds(SubscriptionLagRequest subscriptionLagRequest) {
        return subscriptionLagRequest.getPartitionLagsList().stream()
            .map(PartitionLagRequest::getPartitionId)
            .collect(Collectors.toList());
    }
}
