package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.google.common.collect.PeekingIterator;

public interface PartSubscriber extends Iterable<IterableMessage> {
    PartSubscription getPartSubscription();

    void refreshSubscriberMetadata();

    PeekingIterator<IterableMessage> iterator();

    PeekingIterator<IterableMessage> sidelineIterator();

    PeekingIterator<IterableMessage> retryIterator(int retryQNo);
}
