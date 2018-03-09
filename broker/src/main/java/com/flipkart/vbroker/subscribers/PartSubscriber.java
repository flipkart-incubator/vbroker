package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.iterators.VIterator;

public interface PartSubscriber extends Iterable<IterableMessage> {
    PartSubscription getPartSubscription();

    void refreshSubscriberMetadata();

    VIterator<IterableMessage> iterator();

    VIterator<IterableMessage> sidelineIterator();

    VIterator<IterableMessage> retryIterator(int retryQNo);
}
