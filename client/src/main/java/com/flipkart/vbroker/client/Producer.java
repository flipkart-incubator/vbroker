package com.flipkart.vbroker.client;

import java.io.Closeable;
import java.util.concurrent.CompletionStage;

public interface Producer extends Closeable {

    CompletionStage<MessageMetadata> produce(ProducerRecord producerRecord) throws InterruptedException;

    void close();
}
