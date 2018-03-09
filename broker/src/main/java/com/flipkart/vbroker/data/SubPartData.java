package com.flipkart.vbroker.data;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.iterators.VIterator;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.flipkart.vbroker.subscribers.QType;
import com.flipkart.vbroker.subscribers.SubscriberGroup;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface SubPartData {

    CompletionStage<MessageMetadata> addGroup(SubscriberGroup subscriberGroup);

    CompletionStage<Set<String>> getUniqueGroups();

    CompletionStage<Void> sideline(IterableMessage iterableMessage);

    CompletionStage<Void> retry(IterableMessage iterableMessage);

    VIterator<IterableMessage> getIterator(String groupId);

    VIterator<IterableMessage> getIterator(QType qType);

    CompletionStage<Integer> getLag();
}
