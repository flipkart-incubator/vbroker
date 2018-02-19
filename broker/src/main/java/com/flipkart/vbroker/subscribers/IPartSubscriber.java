package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.google.common.collect.PeekingIterator;

public interface IPartSubscriber extends Iterable<IMessageWithGroup> {
    PartSubscription getPartSubscription();

    void refreshSubscriberMetadata();

    PeekingIterator<IMessageWithGroup> iterator();
}
