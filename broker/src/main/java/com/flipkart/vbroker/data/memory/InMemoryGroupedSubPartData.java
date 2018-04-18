package com.flipkart.vbroker.data.memory;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.data.DefaultGroupedSubPartData;
import com.flipkart.vbroker.iterators.SubscriberGroupIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

@Slf4j
public class InMemoryGroupedSubPartData extends DefaultGroupedSubPartData {

    @Getter
    private final PartSubscription partSubscription;
    private final Map<String, SubscriberGroup> subscriberGroupsMap = new LinkedHashMap<>();
    private final Map<QType, List<String>> failedGroups = new LinkedHashMap<>();
    private final ConcurrentMap<SubscriberGroup, SubscriberGroupIterator<IterableMessage>> groupIteratorMap
        = new ConcurrentHashMap<>();

    public InMemoryGroupedSubPartData(PartSubscription partSubscription) {
        this.partSubscription = partSubscription;
    }

    @Override
    public SubscriberGroup getSubscriberGroup(String groupId) {
        return subscriberGroupsMap.get(groupId);
    }

    @Override
    public SubscriberGroupIterator<IterableMessage> getIterator(SubscriberGroup subscriberGroup, QType qType) {
        return groupIteratorMap.get(subscriberGroup);
    }

    @Override
    public boolean containsIteratorFor(SubscriberGroup subscriberGroup, QType qType) {
        return groupIteratorMap.containsKey(subscriberGroup);
    }

    @Override
    public Stream<SubscriberGroup> getSubscriberGroupStream() {
        return subscriberGroupsMap.values().stream();
    }

    @Override
    public CompletionStage<MessageMetadata> addGroup(SubscriberGroup subscriberGroup) {
        return CompletableFuture.supplyAsync(() -> {
            subscriberGroupsMap.putIfAbsent(subscriberGroup.getGroupId(), subscriberGroup);
            groupIteratorMap.putIfAbsent(subscriberGroup, subscriberGroup.iterator());
            //TODO: we should ideally set msg_id
            return new MessageMetadata(subscriberGroup.getGroupId(), subscriberGroup.getTopicPartition().getTopicId(),
                subscriberGroup.getTopicPartition().getId(), new Random().nextInt());
        });
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {
        return CompletableFuture.supplyAsync(subscriberGroupsMap::keySet);
    }

    @Override
    public CompletionStage<List<String>> getFailedGroups(QType qType) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        List<String> currFailedGroups = failedGroups.computeIfAbsent(qType, qType1 -> new LinkedList<>());
        future.complete(currFailedGroups);
        return future;
    }

    @Override
    public synchronized CompletionStage<Void> appendFailedGroup(IterableMessage iterableMessage) {
        QType qType = iterableMessage.getQType();
        log.debug("Appending failed group {} to QType {}", iterableMessage.getGroupId(), iterableMessage.getQType());

        return getFailedGroups(qType).thenAccept(groups -> {
            List<String> fGroups = failedGroups.get(qType);
            fGroups.add(iterableMessage.getGroupId());
            failedGroups.put(qType, fGroups);

            SubscriberGroup subscriberGroup = getSubscriberGroup(iterableMessage.getGroupId());
            if (!groupIteratorMap.containsKey(subscriberGroup)) {
                groupIteratorMap.putIfAbsent(subscriberGroup, subscriberGroup.iterator());
            }
        });
    }
}
