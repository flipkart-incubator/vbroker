package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.subscribers.MessageWithMetadata;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.google.common.collect.PeekingIterator;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface SubPartData {

    CompletionStage<MessageMetadata> addGroup(SubscriberGroup subscriberGroup);

    CompletionStage<Set<String>> getUniqueGroups();

    CompletionStage<Void> sideline(QType qType, String groupId);

    CompletionStage<Void> retry(QType qType, String groupId);

    PeekingIterator<MessageWithMetadata> getIterator(String groupId);

    Optional<PeekingIterator<MessageWithMetadata>> getIterator(QType qType);
}
