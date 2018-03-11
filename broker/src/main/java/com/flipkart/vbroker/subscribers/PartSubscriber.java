package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.iterators.PartSubscriberIterator;

public interface PartSubscriber {
    PartSubscription getPartSubscription();

    void refreshSubscriberMetadata();

    PartSubscriberIterator<IterableMessage> iterator(QType qType);
}
