package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.iterators.DataIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface SubPartDataManager {

    CompletionStage<MessageMetadata> addGroup(PartSubscription partSubscription, SubscriberGroup subscriberGroup);

    CompletionStage<Set<String>> getUniqueGroups(PartSubscription partSubscription);

    CompletionStage<Void> sideline(PartSubscription partSubscription, IterableMessage iterableMessage);

    CompletionStage<Void> retry(PartSubscription partSubscription, IterableMessage iterableMessage);

    DataIterator<IterableMessage> getIterator(PartSubscription partSubscription, String groupId);

    DataIterator<IterableMessage> getIterator(PartSubscription partSubscription, QType qType);

    List<IterableMessage> poll(PartSubscription partSubscription, QType qType, int maxRecords, long pollTimeMs);

    CompletionStage<Void> commitOffset(PartSubscription partSubscription, String group, int offset);

    CompletionStage<Integer> getOffset(PartSubscription partSubscription, String group);

    CompletionStage<Integer> getLag(PartSubscription partSubscription);
}
