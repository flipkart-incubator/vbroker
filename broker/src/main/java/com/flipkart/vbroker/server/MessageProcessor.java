package com.flipkart.vbroker.server;

import com.flipkart.vbroker.subscribers.IterableMessage;

import java.util.concurrent.CompletionStage;

public interface MessageProcessor {

    CompletionStage<Void> process(IterableMessage messageWithGroup);
}
