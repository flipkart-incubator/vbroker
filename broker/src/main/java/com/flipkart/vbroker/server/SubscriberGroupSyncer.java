package com.flipkart.vbroker.server;

import com.flipkart.vbroker.services.SubscriberMetadataService;
import com.flipkart.vbroker.services.TopicMetadataService;
import com.flipkart.vbroker.subscribers.IPartSubscriber;

import java.io.IOException;
import java.util.List;

//TODO: this is temporary for now until we implement eventing
public class SubscriberGroupSyncer implements Runnable {
    private final List<IPartSubscriber> partSubscribers;
    private final SubscriberMetadataService subscriberMetadataService;
    private final TopicMetadataService topicMetadataService;
    private volatile boolean active = true;

    public SubscriberGroupSyncer(List<IPartSubscriber> partSubscribers, SubscriberMetadataService subscriberMetadataService, TopicMetadataService topicMetadataService) {
        this.partSubscribers = partSubscribers;
        this.subscriberMetadataService = subscriberMetadataService;
        this.topicMetadataService = topicMetadataService;
    }

    @Override
    public void run() {
        while (active) {
            for (IPartSubscriber partSubscriber : partSubscribers) {
                if (!active) {
                    break;
                }
                partSubscriber.refreshSubscriberMetadata();
            }
            try {
                subscriberMetadataService.saveAllSubscribers();
                topicMetadataService.saveAllTopicMetadata();
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
