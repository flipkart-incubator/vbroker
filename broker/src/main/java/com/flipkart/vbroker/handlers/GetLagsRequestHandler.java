package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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
        List<CompletableFuture<SubscriptionLag>> subscriptionLagFutures = IntStream.range(0, getLagsRequest.subscriptionLagsLength())
            .mapToObj(i -> {
                TopicSubscriptionLagRequest subscriptionLagReq = getLagsRequest.subscriptionLags(i);
                short subscriptionId = subscriptionLagReq.subscriptionId();
                short topicId = subscriptionLagReq.topiId();
                List<Short> partitionIds = getPartitionIds(subscriptionLagReq);

                return getSubscriptionLag(subscriptionId, topicId, partitionIds);
            })
            .map(CompletionStage::toCompletableFuture)
            .collect(Collectors.toList());

        return CompletableFuture.allOf(subscriptionLagFutures.toArray(new CompletableFuture[subscriptionLagFutures.size()]))
            .thenApply(aVoid -> subscriptionLagFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList())
            ).thenApply(subscriptionLags -> generateVResponse(subscriptionLags, vRequest.correlationId()));
    }

    private CompletionStage<SubscriptionLag> getSubscriptionLag(short subscriptionId,
                                                                short topicId,
                                                                List<Short> partitionIds) {
        //Get the subscription
        CompletionStage<Subscription> subscriptionStage = subscriptionService.getSubscription(topicId, subscriptionId);

        //For each partition Id, get the corresponding partsubscription
        //NOTE assumes that partitionId = partSubscritptionId.
        List<CompletionStage<PartSubscription>> partSubListStage =
            partitionIds.stream()
                .map(partitionId -> subscriptionStage.thenCompose(subscription -> subscriptionService
                    .getPartSubscription(subscription, partitionId)))
                .collect(Collectors.toList());

        //For each partSubscription, we calculate the lag
        List<CompletableFuture<LagWithPartition>> lagWithPartitionFutures = partSubListStage.stream()
            .map(partSubStage -> partSubStage.thenCompose(this::getPartSubscriberLag))
            .map(CompletionStage::toCompletableFuture)
            .collect(Collectors.toList());


        @SuppressWarnings("unchecked") CompletableFuture<LagWithPartition>[] lagWithPartitionsArray =
            lagWithPartitionFutures.toArray(new CompletableFuture[lagWithPartitionFutures.size()]);

        //Convert Array[Futures] to Future<Array>
        return CompletableFuture.allOf(lagWithPartitionsArray)
            .thenApply(aVoid -> lagWithPartitionFutures
                .stream()
                .map(lagWithPartitionCompletionStage ->
                    lagWithPartitionCompletionStage.toCompletableFuture()
                        .join()) //This is not blocking since it's in thenApply of the combined future
                .collect(Collectors.toList())
            ).thenApply(lagWithPartitions -> new SubscriptionLag(subscriptionId, topicId, lagWithPartitions));
    }

    private CompletionStage<LagWithPartition> getPartSubscriberLag(PartSubscription partSubscription) {
        return subscriptionService.getPartSubscriptionLag(partSubscription)
            .thenApply(lag -> new LagWithPartition(partSubscription.getId(), lag));
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
            short subscriptionId = subscriptionLag.subscriptionId;
            short topicId = subscriptionLag.topicId;
            int partitionLagsVector = buildPartitionLagsVector(builder, subscriptionLag);
            subscriptionLagOffsets[i] = TopicSubscriptionLagRequest.createTopicSubscriptionLagRequest(builder, subscriptionId, topicId, partitionLagsVector);
        }
        return GetLagsResponse.createSubscriptionLagsVector(builder, subscriptionLagOffsets);
    }

    private int buildPartitionLagsVector(FlatBufferBuilder builder, SubscriptionLag subscriptionLag) {
        int[] partitionLags = new int[subscriptionLag.lagWithPartitions.size()];
        for (int i = 0; i < partitionLags.length; i++) {
            short partitionId = subscriptionLag.lagWithPartitions.get(i).partitionId;
            int partitionLag = subscriptionLag.lagWithPartitions.get(i).lag;
            partitionLags[i] = PartitionLag.createPartitionLag(builder, partitionId, partitionLag);
        }
        return TopicSubscriptionLagRequest.createPartitionLagsVector(builder, partitionLags);
    }

    private List<Short> getPartitionIds(TopicSubscriptionLagRequest subscriptionLagRequest) {
        return IntStream.range(0, subscriptionLagRequest.partitionLagsLength())
            .mapToObj(i -> subscriptionLagRequest.partitionLags(i).partitionId())
            .collect(Collectors.toList());
    }

    @AllArgsConstructor
    private class LagWithPartition {
        private short partitionId;
        private int lag;
    }

    private class SubscriptionLag {
        private short subscriptionId;
        private short topicId;
        private List<LagWithPartition> lagWithPartitions;
        public SubscriptionLag(short subscriptionId, short topicId, List<LagWithPartition> lagWithPartitions) {
            this.subscriptionId = subscriptionId;
            this.topicId = topicId;
            this.lagWithPartitions = lagWithPartitions;
        }
    }


}
