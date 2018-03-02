package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.proto.SubscriptionLag;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.utils.CompletionStageUtils;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by kaushal.hooda on 19/02/18.
 */
@Slf4j
@AllArgsConstructor
public class GetLagsRequestHandler implements RequestHandler {
    private final SubscriptionService subscriptionService;
    //private final TopicPartDataManager topicPartDataManager;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        //TODO error handling?
        //TODO stopped subscriptions?
        //TODO messageGroups with no corresponding subscriberGroups? (e.g., in L3?)

        GetLagsRequest getLagsRequest = (GetLagsRequest) vRequest.requestMessage(new GetLagsRequest());
        List<CompletionStage<SubscriptionLag>> subscriptionLagStages = IntStream.range(0, getLagsRequest.subscriptionLagsLength())
            .mapToObj(i -> {
                TopicSubscriptionLagRequest subscriptionLagReq = getLagsRequest.subscriptionLags(i);
                short subscriptionId = subscriptionLagReq.subscriptionId();
                short topicId = subscriptionLagReq.topicId();
                List<Short> partitionIds = getPartitionIds(subscriptionLagReq);
                return getSubscriptionLag(subscriptionId, topicId, partitionIds);
            })
            .collect(Collectors.toList());
        return CompletionStageUtils.listOfStagesToStageOfList(subscriptionLagStages).thenApply(subscriptionLags -> generateVResponse(subscriptionLags, vRequest.correlationId()));
    }

    private CompletionStage<SubscriptionLag> getSubscriptionLag(short subscriptionId,
                                                                short topicId,
                                                                List<Short> partitionIds) {
        //Get the subscription
        CompletionStage<Subscription> subscriptionStage = subscriptionService.getSubscription(topicId, subscriptionId);

        //For each partition Id, get the corresponding partsubscription and its lag.
        //NOTE assumes that partitionId = partSubscritptionId.
//        List<CompletionStage<PartitionLag>> partitionLagStages =
//            partitionIds.stream()
//                .map(partitionId ->
//                    subscriptionStage.thenCompose(subscription -> getPartSubscriberLag(subscription, partitionId)))
//                .collect(Collectors.toList());

        //Collect them in a list
//        List<CompletionStage<PartitionLag>> partitionLagStages = partitionLagStages.stream()
//            .map(partSubStage -> partSubStage.thenCompose(this::getPartSubscriberLag))
//            .collect(Collectors.toList());


        List<CompletionStage<PartitionLag>> partitionLagStages = partitionIds.stream()
            .map(partitionId -> getPartSubscriberLag(subscriptionStage, partitionId))
            .collect(Collectors.toList());
        return CompletionStageUtils.listOfStagesToStageOfList(partitionLagStages)
            .thenApply(lagWithPartitions -> FlatbufUtils.createSubscriptionLag(topicId, subscriptionId, lagWithPartitions));
    }

    private CompletionStage<PartitionLag> getPartSubscriberLag(CompletionStage<Subscription> subscriptionStage, short partitionId) {
        return subscriptionStage
            .thenCompose(subscription -> subscriptionService.getPartSubscription(subscription, partitionId))
            .thenCompose(subscriptionService::getPartSubscriptionLag)
            .thenApply(lag -> {
                VStatus vStatus = FlatbufUtils.createVStatus(StatusCode.Success, "");
                return FlatbufUtils.createPartitionLag(partitionId, lag, vStatus);
            })
            .exceptionally(throwable -> {
                log.debug("Catching an exception");
                VStatus vStatus = FlatbufUtils.createVStatus(StatusCode.GetLagFailed, throwable.getMessage());
                return FlatbufUtils.createPartitionLag(partitionId, -1, vStatus);
            });
    }

    private VResponse generateVResponse(List<SubscriptionLag> subscriptionLags, int correlationId) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int subscriptionLagsVector = buildSubscriptionLagVector(subscriptionLags, builder);
        int getLagsResponse = GetLagsResponse.createGetLagsResponse(builder, subscriptionLagsVector);
        int vResponse = VResponse.createVResponse(builder, correlationId, ResponseMessage.GetLagsResponse, getLagsResponse);
        builder.finish(vResponse);
        return VResponse.getRootAsVResponse(builder.dataBuffer());
    }

    private int buildSubscriptionLagVector(List<SubscriptionLag> subscriptionLags, FlatBufferBuilder builder) {
        int[] subscriptionLagOffsets = new int[subscriptionLags.size()];
        for (int i = 0; i < subscriptionLags.size(); i++) {
            SubscriptionLag subscriptionLag = subscriptionLags.get(i);
            short subscriptionId = subscriptionLag.subscriptionId();
            short topicId = subscriptionLag.topicId();
            int partitionLagsVector = buildPartitionLagsVector(builder, subscriptionLag);
            subscriptionLagOffsets[i] = SubscriptionLag.createSubscriptionLag(builder, subscriptionId, topicId, partitionLagsVector);
        }
        return GetLagsResponse.createSubscriptionLagsVector(builder, subscriptionLagOffsets);
    }

    private int buildPartitionLagsVector(FlatBufferBuilder builder, SubscriptionLag subscriptionLag) {
        int[] partitionLags = new int[subscriptionLag.partitionLagsLength()];
        for (int i = 0; i < subscriptionLag.partitionLagsLength(); i++) {
            short partitionId = subscriptionLag.partitionLags(i).partitionId();
            int partitionLag = subscriptionLag.partitionLags(i).lag();
            VStatus status = subscriptionLag.partitionLags(i).status();
            partitionLags[i] = FlatbufUtils.buildPartitionLag(builder, partitionId, partitionLag, status);
        }
        return SubscriptionLag.createPartitionLagsVector(builder, partitionLags);
    }

    private List<Short> getPartitionIds(TopicSubscriptionLagRequest subscriptionLagRequest) {
        return IntStream.range(0, subscriptionLagRequest.partitionLagsLength())
            .mapToObj(i -> subscriptionLagRequest.partitionLags(i).partitionId())
            .collect(Collectors.toList());
    }

//    @AllArgsConstructor
//    private class LagWithPartition {
//        private short partitionId;
//        private int lag;
//    }
//
//    private class SubscriptionLag {
//        private short subscriptionId;
//        private short topicId;
//        private List<LagWithPartition> lagWithPartitions;
//
//        public SubscriptionLag(short subscriptionId, short topicId, List<LagWithPartition> lagWithPartitions) {
//            this.subscriptionId = subscriptionId;
//            this.topicId = topicId;
//            this.lagWithPartitions = lagWithPartitions;
//        }
//    }


}
