package com.flipkart.vbroker.services;

import com.flipkart.vbroker.wrappers.Queue;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Created by kaushal.hooda on 06/03/18.
 */
public interface QueueService {

    public CompletionStage<Queue> getQueue(int queueId);

    public CompletionStage<List<Queue>> getAllQueues();

    public CompletionStage<Queue> createQueue(Queue queue);

    public CompletionStage<Queue> createQueueAdmin(Queue queue);
}
