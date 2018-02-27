package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.Topic;

import java.io.Closeable;
import java.util.concurrent.CompletionStage;

public interface Producer extends Closeable {

    CompletionStage<MessageMetadata> produce(Message message, Topic topic) throws InterruptedException;

    void close();
}
