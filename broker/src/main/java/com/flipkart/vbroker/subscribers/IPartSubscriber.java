package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.google.common.collect.PeekingIterator;

public interface IPartSubscriber extends Iterable<MessageWithMetadata> {
    PartSubscription getPartSubscription();

    void refreshSubscriberMetadata();

    PeekingIterator<MessageWithMetadata> iterator();

    PeekingIterator<MessageWithMetadata> sidelineIterator();

    PeekingIterator<MessageWithMetadata> retryIterator(int retryQNo);
}
