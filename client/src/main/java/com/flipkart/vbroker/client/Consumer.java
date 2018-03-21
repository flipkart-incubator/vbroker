package com.flipkart.vbroker.client;

import com.flipkart.vbroker.wrappers.Subscription;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface Consumer extends AutoCloseable {

    void subscribe(Set<Subscription> subscriptions);

    void unSubscribe(Set<Subscription> subscriptions);

    CompletionStage<List<ConsumerRecord>> poll(int maxRecords, int timeoutMs);

    CompletionStage<Void> commitOffset(String group, int offset);

    CompletionStage<Integer> getOffset(String group);
}
