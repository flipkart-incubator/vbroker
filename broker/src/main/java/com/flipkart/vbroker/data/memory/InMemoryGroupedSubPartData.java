package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.SubPartData;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.subscribers.MessageWithMetadata;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.google.common.collect.PeekingIterator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.flipkart.vbroker.subscribers.QType.*;

@Slf4j
public class InMemoryGroupedSubPartData implements SubPartData {

    @Getter
    private final PartSubscription partSubscription;
    private final Map<String, SubscriberGroup> subscriberGroupsMap = new LinkedHashMap<>();
    private final Map<SubscriberGroup, PeekingIterator<MessageWithMetadata>> subscriberGroupIteratorMap = new LinkedHashMap<>();
    private final Map<QType, List<String>> failedGroups = new LinkedHashMap<>();

    public InMemoryGroupedSubPartData(PartSubscription partSubscription) {
        this.partSubscription = partSubscription;
    }

    @Override
    public CompletionStage<MessageMetadata> addGroup(SubscriberGroup subscriberGroup) {
        return CompletableFuture.supplyAsync(() -> {
            subscriberGroupsMap.put(subscriberGroup.getGroupId(), subscriberGroup);
            subscriberGroupIteratorMap.put(subscriberGroup, subscriberGroup.iterator());

            return new MessageMetadata(subscriberGroup.getTopicPartition().getTopicId(),
                subscriberGroup.getTopicPartition().getId(), new Random().nextInt());
        });
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {
        return CompletableFuture.supplyAsync(subscriberGroupsMap::keySet);
    }

    private CompletionStage<List<String>> getFailedGroups(QType qType) {
        return CompletableFuture.supplyAsync(() -> {
            failedGroups.computeIfAbsent(qType, qType1 -> new LinkedList<>());
            return failedGroups.get(qType);
        });
    }

    private List<String> getFailedGroupsByBlocking(MessageWithMetadata messageWithMetadata) {
        failedGroups.computeIfAbsent(messageWithMetadata.getQType(), qType1 -> new LinkedList<>());
        return failedGroups.get(messageWithMetadata.getQType());
    }

    @Override
    public CompletionStage<Void> sideline(MessageWithMetadata messageWithMetadata) {
        return getFailedGroups(messageWithMetadata.getQType()).thenApplyAsync(groups -> {
            groups.add(messageWithMetadata.getGroupId());
            return null;
        });
    }

    @Override
    public CompletionStage<Void> retry(MessageWithMetadata messageWithMetadata) {
        QType destinationQType;
        switch (messageWithMetadata.getQType()) {
            case MAIN:
                destinationQType = RETRY_1;
                break;
            case RETRY_1:
                destinationQType = RETRY_2;
                break;
            case RETRY_2:
                destinationQType = RETRY_3;
                break;
            case RETRY_3:
                destinationQType = QType.SIDELINE;
                break;
            default:
                throw new VBrokerException("Unknown QType: " + messageWithMetadata.getQType());
        }

        return getFailedGroups(destinationQType).thenApplyAsync(groups -> {
            groups.add(messageWithMetadata.getGroupId());
            return null;
        });
    }

    @Override
    public PeekingIterator<MessageWithMetadata> getIterator(String groupId) {
        SubscriberGroup subscriberGroup = subscriberGroupsMap.get(groupId);
        return subscriberGroupIteratorMap.get(subscriberGroup);
    }

    @Override
    public Optional<PeekingIterator<MessageWithMetadata>> getIterator(QType qType) {
        CompletionStage<List<String>> values;

        switch (qType) {
            case MAIN:
                values = CompletableFuture.supplyAsync(() -> subscriberGroupsMap.values()
                    .stream()
                    .map(SubscriberGroup::getGroupId)
                    .collect(Collectors.toList()));
                break;
            default:
                values = getFailedGroups(qType);
                break;
        }

        if (log.isDebugEnabled()) {
            List<String> groupIds = values.toCompletableFuture().join() //FIX this!!
                .stream()
                .map(subscriberGroupsMap::get)
                .map(SubscriberGroup::getGroupId)
                .collect(Collectors.toList());
            log.debug("SubscriberGroupsMap values: {}", Collections.singletonList(groupIds));
        }

        return values.toCompletableFuture().join() //FIX this!
            .stream()
            .map(subscriberGroupsMap::get)
            .filter(group -> !group.isLocked())
            .filter(group -> qType.equals(group.getQType()))
            .filter(subscriberGroupIteratorMap::containsKey)
            .map(subscriberGroupIteratorMap::get)
            .filter(Iterator::hasNext)
            .findFirst();
    }

    @Override
    public CompletionStage<Integer> getCurSeqNo(String groupId) {
        return CompletableFuture.completedFuture(subscriberGroupsMap.get(groupId).getCurrSeqNo().get());
    }

    @Override
    public CompletionStage<Integer> getLag() {
        return getUniqueGroups().thenCompose(groups -> {
            @SuppressWarnings("unchecked") CompletableFuture<Integer>[] lagFutures = groups.stream()
                .map(subscriberGroupsMap::get)
                .map(SubscriberGroup::getLag)
                .map(CompletionStage::toCompletableFuture)
                .toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(lagFutures).thenApply(aVoid ->
                Arrays.stream(lagFutures)
                    .map(CompletableFuture::join)
                    .reduce(0, (lag1, lag2) -> lag1 + lag2));
        });
    }
}
