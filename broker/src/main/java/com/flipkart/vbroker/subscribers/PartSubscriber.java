package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface PartSubscriber {
    PartSubscription getPartSubscription();

    void refreshSubscriberMetadata();

    PartSubscriberIterator<IterableMessage> iterator(QType qType);

    List<IterableMessage> poll(QType qType, int maxRecords, long pollTimeMs);

    CompletionStage<Void> commitOffset(String group, int offset);

    CompletionStage<Integer> getOffset(String group);
}
