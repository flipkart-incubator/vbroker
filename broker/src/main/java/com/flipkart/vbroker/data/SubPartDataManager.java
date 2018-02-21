package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.subscribers.MessageWithMetadata;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;
import com.google.common.collect.PeekingIterator;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface SubPartDataManager {

    CompletionStage<MessageMetadata> addGroup(PartSubscription partSubscription, SubscriberGroup subscriberGroup);

    CompletionStage<Set<String>> getUniqueGroups(PartSubscription partSubscription);

    CompletionStage<Void> sideline(PartSubscription partSubscription, MessageWithMetadata messageWithMetadata);

    CompletionStage<Void> retry(PartSubscription partSubscription, MessageWithMetadata messageWithMetadata);

    PeekingIterator<MessageWithMetadata> getIterator(PartSubscription partSubscription, String groupId);

    Optional<PeekingIterator<MessageWithMetadata>> getIterator(PartSubscription partSubscription, QType qType);

    CompletionStage<Integer> getCurrSeqNo(PartSubscription partSubscription, String groupId);

    CompletionStage<Integer> getLag(PartSubscription partSubscription);
}
