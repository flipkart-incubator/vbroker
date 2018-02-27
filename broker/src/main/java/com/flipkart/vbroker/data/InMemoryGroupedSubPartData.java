package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;
import com.flipkart.vbroker.server.MessageUtils;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Table;
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
    private final Map<QType, List<String>> failedGroups = new LinkedHashMap<>();
    private final Table<SubscriberGroup, QType, PeekingIterator<IterableMessage>> groupQTypeIteratorTable = HashBasedTable.create();

    //private final Map<SubscriberGroup, PeekingIterator<IterableMessage>> subscriberGroupIteratorMap = new LinkedHashMap<>();
    //private final Map<QType, PartSubscriberIterator> qTypeIterators = new ConcurrentHashMap<>();

    public InMemoryGroupedSubPartData(PartSubscription partSubscription) {
        this.partSubscription = partSubscription;
    }

    @Override
    public CompletionStage<MessageMetadata> addGroup(SubscriberGroup subscriberGroup) {
        return CompletableFuture.supplyAsync(() -> {
            subscriberGroupsMap.put(subscriberGroup.getGroupId(), subscriberGroup);
            //subscriberGroupIteratorMap.put(subscriberGroup, subscriberGroup.iterator());
            groupQTypeIteratorTable.put(subscriberGroup, QType.MAIN, subscriberGroup.iterator());

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
            return failedGroups.computeIfAbsent(qType, qType1 -> new LinkedList<>());
            //return failedGroups.get(qType);
        });
    }

    private CompletionStage<List<String>> computeListIfAbsent(QType qType) {
        return CompletableFuture.supplyAsync(() -> failedGroups.computeIfAbsent(qType, qType1 -> new LinkedList<>()));
    }

    private List<String> getFailedGroupsByBlocking(IterableMessage iterableMessage) {
        failedGroups.computeIfAbsent(iterableMessage.getQType(), qType1 -> new LinkedList<>());
        return failedGroups.get(iterableMessage.getQType());
    }

    @Override
    public CompletionStage<Void> sideline(IterableMessage iterableMessage) {
        iterableMessage.setQType(QType.SIDELINE);
        return appendFailedGroup(iterableMessage);
    }

    @Override
    public CompletionStage<Void> retry(IterableMessage iterableMessage) {
        QType destinationQType = MessageUtils.getNextRetryQType(iterableMessage.getQType());
        iterableMessage.setQType(destinationQType);

        return appendFailedGroup(iterableMessage);
    }

    private synchronized CompletionStage<Void> appendFailedGroup(IterableMessage iterableMessage) {
        QType qType = iterableMessage.getQType();
        return getFailedGroups(qType).thenAccept(groups -> {
            List<String> fGroups = failedGroups.get(qType);
            fGroups.add(iterableMessage.getGroupId());
            failedGroups.put(qType, fGroups);

            SubscriberGroup subscriberGroup = subscriberGroupsMap.get(iterableMessage.getGroupId());
            if (!groupQTypeIteratorTable.contains(subscriberGroup, qType)) {
                //don't create a new iterator if already failed iterator exists
                groupQTypeIteratorTable.put(subscriberGroup, qType, subscriberGroup.iterator(qType));
            }
        });
    }

    @Override
    public PeekingIterator<IterableMessage> getIterator(String groupId) {
        SubscriberGroup subscriberGroup = subscriberGroupsMap.get(groupId);
        return groupQTypeIteratorTable.get(subscriberGroup, QType.MAIN);
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

        List<String> valuesComputed = values.toCompletableFuture().join();  //FIX this!
        if (log.isDebugEnabled()) {
            List<String> groupIds = valuesComputed //FIX this!!
                .stream()
                .map(subscriberGroupsMap::get)
                .map(SubscriberGroup::getGroupId)
                .collect(Collectors.toList());
            log.debug("SubscriberGroupsMap values for qType {} are: {}", qType, Collections.singletonList(groupIds));
        }

        Optional<PeekingIterator<IterableMessage>> iterator = valuesComputed
            .stream()
            .map(subscriberGroupsMap::get)
            .filter(group -> !group.isLocked())
            .filter(group -> qType.equals(group.getQType()))
            .filter(group -> groupQTypeIteratorTable.contains(group, qType))
            .map(subscriberGroup -> groupQTypeIteratorTable.get(subscriberGroup, qType))
            .filter(Iterator::hasNext)
            .findFirst();

        return Optional.of(new PartSubscriberIterator() {
            @Override
            protected Optional<PeekingIterator<IterableMessage>> nextIterator() {
                return iterator;
            }
        });
    }
}
