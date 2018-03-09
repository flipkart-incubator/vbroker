package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.iterators.VIterator;

public interface PartSubscriber {
    PartSubscription getPartSubscription();

    void refreshSubscriberMetadata();

    VIterator<IterableMessage> iterator(QType qType);
}
