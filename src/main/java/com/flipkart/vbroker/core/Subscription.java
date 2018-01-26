package com.flipkart.vbroker.core;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hooda on 19/1/18
 */

@Getter
public class Subscription {
    private final short id;
    private final Topic topic;
    private final List<PartSubscription> partSubscriptions = new LinkedList<>();
    @Setter
    private boolean grouped = true;

    public Subscription(short id,
                        Topic topic,
                        boolean grouped) {
        this.id = id;
        this.topic = topic;
        this.grouped = grouped;
    }

    public void addPartSubscription(PartSubscription partSubscription) {
        this.partSubscriptions.add(partSubscription);
    }

    public PartSubscription getPartSubscription(int partSubscriptionId) {
        return partSubscriptions.get(partSubscriptionId);
    }
}