package com.flipkart.vbroker.core;

import java.util.Iterator;
import java.util.List;

public class Subscriber implements Iterable<SubscriptionGroup> {

    private List<SubscriptionGroup> subscriptionGroups;

    @Override
    public Iterator<SubscriptionGroup> iterator() {
        return null;
    }
}
