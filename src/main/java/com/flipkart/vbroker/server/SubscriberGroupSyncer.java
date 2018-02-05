package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.PartSubscriber;
import com.flipkart.vbroker.services.SubscriberMetadataService;

import java.io.IOException;
import java.util.List;

//TODO: this is temporary for now until we implement eventing
public class SubscriberGroupSyncer implements Runnable {
    private final List<PartSubscriber> partSubscribers;
    private final SubscriberMetadataService subscriberMetadataService;
    private volatile boolean active = true;

    public SubscriberGroupSyncer(List<PartSubscriber> partSubscribers, SubscriberMetadataService subscriberMetadataService) {
        this.partSubscribers = partSubscribers;
        this.subscriberMetadataService = subscriberMetadataService;
    }

    @Override
    public void run() {
        while (active) {
            for (PartSubscriber partSubscriber : partSubscribers) {
                partSubscriber.refreshSubscriberGroups();
            }
            try {
                subscriberMetadataService.saveAllSubscribers();
            } catch (IOException ignored) {
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void stop() {
        active = false;
    }
}
