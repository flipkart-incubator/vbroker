package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.server.MessageUtils;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.google.common.collect.PeekingIterator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryGroupedSubPartData implements SubPartData {

    @Getter
    private final PartSubscription partSubscription;
    private final Map<String, SubscriberGroup> subscriberGroupsMap = new LinkedHashMap<>();
    private final Map<SubscriberGroup, PeekingIterator<IterableMessage>> subscriberGroupIteratorMap = new LinkedHashMap<>();
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

    private List<String> getFailedGroupsByBlocking(IterableMessage iterableMessage) {
        failedGroups.computeIfAbsent(iterableMessage.getQType(), qType1 -> new LinkedList<>());
        return failedGroups.get(iterableMessage.getQType());
    }

    @Override
    public CompletionStage<Void> sideline(IterableMessage iterableMessage) {
        return getFailedGroups(iterableMessage.getQType()).thenApplyAsync(groups -> {
            groups.add(iterableMessage.getGroupId());
            return null;
        });
    }

    @Override
    public CompletionStage<Void> retry(IterableMessage iterableMessage) {
        QType destinationQType = MessageUtils.getNextRetryQType(iterableMessage.getQType());
        iterableMessage.setQType(destinationQType);
        return getFailedGroups(iterableMessage.getQType()).thenApplyAsync(groups -> {
            groups.add(iterableMessage.getGroupId());
            return null;
        });
    }

    @Override
    public PeekingIterator<IterableMessage> getIterator(String groupId) {
        SubscriberGroup subscriberGroup = subscriberGroupsMap.get(groupId);
        return subscriberGroupIteratorMap.get(subscriberGroup);
    }

    @Override
    public Optional<PeekingIterator<IterableMessage>> getIterator(QType qType) {
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
}
