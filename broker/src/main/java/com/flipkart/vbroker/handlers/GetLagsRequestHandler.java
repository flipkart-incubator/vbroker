package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.services.SubscriptionService;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return CompletableFuture.supplyAsync(() -> processGetLagsRequest(vRequest));
    }

    private VResponse processGetLagsRequest(VRequest vRequest) {
        //TODO error handling?
        //TODO stopped subscriptions?
        //TODO messageGroups with no corresponding subscriberGroups? (e.g., in L3?)

        //

        GetLagsRequest getLagsRequest = (GetLagsRequest) vRequest.requestMessage(new GetLagsRequest());
        Map<Short, Map<Short, Integer>> subIdToLagMap = new HashMap<>();
        for (int i = 0; i < getLagsRequest.subscriptionLagsLength(); i++) {
            TopicSubscriptionLagRequest subscriptionLagReq = getLagsRequest.subscriptionLags(i);
            short subscriptionId = subscriptionLagReq.subscriptionId();
            short topicId = subscriptionLagReq.topiId();
            List<Short> partitionIds = getPartitionIds(subscriptionLagReq);

            getSubscriptionLag(subscriptionId, topicId, partitionIds);
            //addToSubIdToLagMap(subIdToLagMap, subscriptionId, partitionIdToLagMap);
        }

        List<CompletionStage<List<LagWithPartition>>> collect = IntStream.range(0, getLagsRequest.subscriptionLagsLength())
            .mapToObj(getLagsRequest::subscriptionLags)
            .map(topicSubscriptionLagRequest -> getSubscriptionLag(topicSubscriptionLagRequest.subscriptionId(),
                topicSubscriptionLagRequest.topiId(),
                getPartitionIds(topicSubscriptionLagRequest)))
            .collect(Collectors.toList());


        return generateVResponse(getLagsRequest, subIdToLagMap, vRequest.correlationId());
    }

    private CompletionStage<List<LagWithPartition>> getSubscriptionLag(short subscriptionId,
                                                                       short topicId,
                                                                       List<Short> partitionIds) {
        CompletionStage<Subscription> subscriptionStage = subscriptionService.getSubscription(topicId, subscriptionId);
        List<CompletionStage<PartSubscription>> partSubListStage =
            partitionIds
                .stream()
                .map(partitionId -> subscriptionStage.thenCompose(subscription -> subscriptionService
                    .getPartSubscription(subscription, partitionId)))
                .collect(Collectors.toList());
        List<CompletionStage<LagWithPartition>> lagWithPartitionStageList = partSubListStage.stream()
            .map(partSubStage -> partSubStage.thenCompose(this::getPartSubscriberLag))
            .collect(Collectors.toList());
        @SuppressWarnings({"unchecked", "SuspiciousToArrayCall"})
        CompletableFuture<LagWithPartition>[] lagWithPartitionsArray =
            lagWithPartitionStageList.toArray(new CompletableFuture[lagWithPartitionStageList.size()]);

//        return CompletableFuture.allOf(lagWithPartitionsArray)
//            .thenCompose(ignored -> CompletableFuture.supplyAsync(() -> lagWithPartitionStageList
//                .stream()
//                .map(stage -> stage.toCompletableFuture().join())
//                .collect(Collectors.toList())));
//
        return CompletableFuture.allOf(lagWithPartitionsArray)
            .thenApply(aVoid -> lagWithPartitionStageList
                .stream()
                .map(lagWithPartitionCompletionStage -> lagWithPartitionCompletionStage.toCompletableFuture().join())
                .collect(Collectors.toList()));
    }

    private CompletionStage<LagWithPartition> getPartSubscriberLag(PartSubscription partSubscription) {
        return CompletableFuture.completedFuture(new LagWithPartition((short) 1, 1));
    }

//    private int getPartSubscriberLag(PartSubscriber partSubscriber) {
//        int lag = 0;
//        for (SubscriberGroup group : partSubscriber.getSubscriberGroupsMap().values()) {
//            int messages = topicPartDataManager.getCurrentOffset(group.getTopicPartition(), group.getGroupId()).toCompletableFuture().join();
//            lag += messages - group.getCurrSeqNo().get();
//        }
//        return lag;
//    }

    private void addToSubIdToLagMap(Map<Short, Map<Short, Integer>> subIdToLagMap, short subscriptionId, Map<Short, Integer> partitionIdToLagMap) {
        if (!subIdToLagMap.containsKey(subscriptionId)) {
            subIdToLagMap.put(subscriptionId, partitionIdToLagMap);
        } else {
            subIdToLagMap.get(subscriptionId).putAll(partitionIdToLagMap);
        }
    }

    private VResponse generateVResponse(GetLagsRequest getLagsRequest, Map<Short, Map<Short, Integer>> subIdToLagMap, int correlationId) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int subscriptionLagsVector = buildSubscriptionLagVector(getLagsRequest, subIdToLagMap, builder);
        int getLagsResponse = GetLagsResponse.createGetLagsResponse(builder, subscriptionLagsVector);
        int vResponse = VResponse.createVResponse(builder, correlationId, ResponseMessage.GetLagsResponse, getLagsResponse);
        builder.finish(vResponse);
        return VResponse.getRootAsVResponse(builder.dataBuffer());
    }

    private int buildSubscriptionLagVector(GetLagsRequest getLagsRequest, Map<Short, Map<Short, Integer>> subIdToLagMap, FlatBufferBuilder builder) {
        int[] subscriptionLags = new int[getLagsRequest.subscriptionLagsLength()];
        for (int i = 0; i < getLagsRequest.subscriptionLagsLength(); i++) {
            TopicSubscriptionLagRequest subscriptionLagReq = getLagsRequest.subscriptionLags(i);
            short subscriptionId = subscriptionLagReq.subscriptionId();
            short topicId = subscriptionLagReq.topiId();
            int partitionLagsVector = buildPartitionLagsVector(subIdToLagMap.get(subscriptionId), builder, subscriptionLagReq);
            subscriptionLags[i] = TopicSubscriptionLagRequest.createTopicSubscriptionLagRequest(builder, subscriptionId, topicId, partitionLagsVector);
        }
        return GetLagsResponse.createSubscriptionLagsVector(builder, subscriptionLags);
    }

    private int buildPartitionLagsVector(Map<Short, Integer> partitionIdToLagMap, FlatBufferBuilder builder, TopicSubscriptionLagRequest subscriptionLagReq) {
        int[] partitionLags = new int[subscriptionLagReq.partitionLagsLength()];
        for (int i = 0; i < subscriptionLagReq.partitionLagsLength(); i++) {
            short partitionId = subscriptionLagReq.partitionLags(i).partitionId();
            int partitionLag = partitionIdToLagMap.get(partitionId);
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
    public class LagWithPartition {
        private short partitionId;
        private int lag;
    }

}
