package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.iterators.VIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface SubPartDataManager {

    CompletionStage<MessageMetadata> addGroup(PartSubscription partSubscription, SubscriberGroup subscriberGroup);

    CompletionStage<Set<String>> getUniqueGroups(PartSubscription partSubscription);

    CompletionStage<Void> sideline(PartSubscription partSubscription, IterableMessage iterableMessage);

    CompletionStage<Void> retry(PartSubscription partSubscription, IterableMessage iterableMessage);

    VIterator<IterableMessage> getIterator(PartSubscription partSubscription, String groupId);

    VIterator<IterableMessage> getIterator(PartSubscription partSubscription, QType qType);

    CompletionStage<Integer> getLag(PartSubscription partSubscription);
}
