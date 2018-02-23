package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.google.common.collect.PeekingIterator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class InMemorySubPartDataManager implements SubPartDataManager {

    private final TopicPartDataManager topicPartDataManager;
    private final Map<PartSubscription, SubPartData> dataMap = new LinkedHashMap<>();

    public InMemorySubPartDataManager(TopicPartDataManager topicPartDataManager) {
        this.topicPartDataManager = topicPartDataManager;
    }

    private CompletionStage<SubPartData> getSubPartDataAsync(PartSubscription partSubscription) {
        return CompletableFuture.supplyAsync(() -> {
            dataMap.computeIfAbsent(partSubscription, partSubscription1 -> {
                SubPartData subPartData;
                if (partSubscription1.isGrouped()) {
                    subPartData = new InMemoryGroupedSubPartData(partSubscription1);
                } else {
                    subPartData = new InMemoryUnGroupedSubPartData(partSubscription1, topicPartDataManager);
                }
                return subPartData;
            });
            return dataMap.get(partSubscription);
        });
    }

    @Override
    public CompletionStage<MessageMetadata> addGroup(PartSubscription partSubscription, SubscriberGroup subscriberGroup) {
        return getSubPartDataAsync(partSubscription).thenCompose(subPartData -> subPartData.addGroup(subscriberGroup));
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups(PartSubscription partSubscription) {
        return getSubPartDataAsync(partSubscription).thenCompose(SubPartData::getUniqueGroups);
    }

    @Override
    public CompletionStage<Void> sideline(PartSubscription partSubscription, IterableMessage iterableMessage) {
        return getSubPartDataAsync(partSubscription).thenCompose(subPartData -> subPartData.sideline(iterableMessage));
    }

    @Override
    public CompletionStage<Void> retry(PartSubscription partSubscription, IterableMessage iterableMessage) {
        return getSubPartDataAsync(partSubscription).thenCompose(subPartData -> subPartData.retry(iterableMessage));
    }

    @Override
    public PeekingIterator<IterableMessage> getIterator(PartSubscription partSubscription, String groupId) {
        return getSubPartDataAsync(partSubscription).thenApplyAsync(subPartData -> subPartData.getIterator(groupId))
            .toCompletableFuture().join(); //TODO: fix this!
    }

    @Override
    public Optional<PeekingIterator<IterableMessage>> getIterator(PartSubscription partSubscription, QType qType) {
        return getSubPartDataAsync(partSubscription).thenApplyAsync(subPartData -> subPartData.getIterator(qType))
            .toCompletableFuture().join(); //TODO: fix this!
    }
}
