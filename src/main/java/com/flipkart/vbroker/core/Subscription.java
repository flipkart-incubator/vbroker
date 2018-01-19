package com.flipkart.vbroker.core;

import java.util.List;

/**
 * Created by hooda on 19/1/18
 */

public class Subscription {
    private short id;
    private boolean grouped = false;
    private Topic topic;
    List<SubscriptionGroup> subscriptionGroups;
}