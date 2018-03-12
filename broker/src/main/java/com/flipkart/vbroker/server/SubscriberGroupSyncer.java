package com.flipkart.vbroker.server;

import com.flipkart.vbroker.subscribers.PartSubscriber;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

//TODO: this is temporary for now until we implement eventing
@Slf4j
public class SubscriberGroupSyncer implements Runnable {
    private final List<PartSubscriber> partSubscribers;
    private volatile boolean active = true;

    public SubscriberGroupSyncer(List<PartSubscriber> partSubscribers) {
        this.partSubscribers = partSubscribers;
    }

    @Override
    public void run() {
        while (active) {
            for (PartSubscriber partSubscriber : partSubscribers) {
                if (!active) {
                    break;
                }

                log.debug("Refreshing subscriber metadata");
                partSubscriber.refreshSubscriberMetadata();
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
