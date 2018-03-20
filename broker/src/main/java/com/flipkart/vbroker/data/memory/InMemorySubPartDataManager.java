package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartData;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.iterators.DataIterator;
import com.flipkart.vbroker.server.MessageUtils;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
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
        log.info("Sidelining message {} for partSubscription {}", iterableMessage.getMessage().messageId(), partSubscription);
        iterableMessage.setQType(QType.SIDELINE);
        return getSubPartDataAsync(partSubscription).thenCompose(subPartData -> subPartData.sideline(iterableMessage));
    }

    @Override
    public CompletionStage<Void> retry(PartSubscription partSubscription, IterableMessage iterableMessage) {
        log.info("Retrying message with msg_id {} for part-subscription {}", iterableMessage.getMessage().messageId(), partSubscription);
        QType destinationQType = MessageUtils.getNextRetryQType(iterableMessage.getQType());
        iterableMessage.setQType(destinationQType); //TODO: find a better way instead of mutating an argument
        return getSubPartDataAsync(partSubscription).thenCompose(subPartData -> subPartData.retry(iterableMessage));
    }

    @Override
    public DataIterator<IterableMessage> getIterator(PartSubscription partSubscription, String groupId) {
        return getSubPartDataAsync(partSubscription).thenApplyAsync(subPartData -> subPartData.getIterator(groupId))
            .toCompletableFuture().join(); //TODO: fix this!
    }

    @Override
    public DataIterator<IterableMessage> getIterator(PartSubscription partSubscription, QType qType) {
        return getSubPartDataAsync(partSubscription).thenApplyAsync(subPartData -> subPartData.getIterator(qType))
            .toCompletableFuture().join(); //TODO: fix this!
    }

    @Override
    public List<IterableMessage> poll(PartSubscription partSubscription, QType qType, int maxRecords, long pollTimeMs) {
        return getSubPartDataAsync(partSubscription).thenApply(subPartData ->
            subPartData.poll(qType, maxRecords, pollTimeMs)).toCompletableFuture().join();
    }

    @Override
    public CompletionStage<Void> commitOffset(PartSubscription partSubscription, String group, int offset) {
        return getSubPartDataAsync(partSubscription).thenApply(subPartData ->
            subPartData.commitOffset(group, offset)).toCompletableFuture().join();
    }

    @Override
    public CompletionStage<Integer> getOffset(PartSubscription partSubscription, String group) {
        return getSubPartDataAsync(partSubscription).thenApply(subPartData ->
            subPartData.getOffset(group)).toCompletableFuture().join();
    }

    @Override
    public CompletionStage<Integer> getLag(PartSubscription partSubscription) {
        return getSubPartDataAsync(partSubscription).thenCompose(SubPartData::getLag);
    }
}
