package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.subscribers.MessageWithMetadata;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.google.common.collect.PeekingIterator;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUnGroupedSubPartData implements SubPartData {

    private final PartSubscription partSubscription;
    private final Map<QType, List<MessageWithMetadata>> failedMessagesMap = new ConcurrentHashMap<>();

    public InMemoryUnGroupedSubPartData(PartSubscription partSubscription) {
        this.partSubscription = partSubscription;
    }

    @Override
    public CompletionStage<MessageMetadata> addGroup(SubscriberGroup subscriberGroup) {
        throw new UnsupportedOperationException("You cannot add SubscriberGroup to an un-grouped subscription");
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {
        throw new UnsupportedOperationException("You cannot get unique groups to an un-grouped subscription");
    }

    private CompletionStage<List<MessageWithMetadata>> getFailedMessages(QType qType) {
        return CompletableFuture.supplyAsync(() -> {
            failedMessagesMap.computeIfAbsent(qType, qType1 -> new LinkedList<>());
            return failedMessagesMap.get(qType);
        });
    }

    @Override
    public CompletionStage<Void> sideline(MessageWithMetadata messageWithMetadata) {
        return getFailedMessages(messageWithMetadata.getQType())
            .thenAccept(messages -> messages.add(messageWithMetadata));
    }

    @Override
    public CompletionStage<Void> retry(MessageWithMetadata messageWithMetadata) {
        return getFailedMessages(messageWithMetadata.getQType())
            .thenAccept(messages -> messages.add(messageWithMetadata));
    }

    @Override
    public PeekingIterator<MessageWithMetadata> getIterator(String groupId) {
        throw new UnsupportedOperationException("You cannot get groupId level iterator for a un-grouped subscription");
    }

    @Override
    public Optional<PeekingIterator<MessageWithMetadata>> getIterator(QType qType) {
        return Optional.empty();
    }
}
