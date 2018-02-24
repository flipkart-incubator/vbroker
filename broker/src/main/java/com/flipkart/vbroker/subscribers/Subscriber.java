package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Subscriber {

    @Getter
    private final String subscriberId;
    private final Map<PartSubscription, PartSubscriber> map = new LinkedHashMap<>();

    public Subscriber(String subscriberId, List<PartSubscriber> partSubscribers) {
        this.subscriberId = subscriberId;
        for (PartSubscriber partSubscriber : partSubscribers) {
            this.map.put(partSubscriber.getPartSubscription(), partSubscriber);
        }
    }

    public PartSubscriber getPartSubscriber(PartSubscription partSubscription) {
        return this.map.get(partSubscription);
    }
}
