package com.flipkart.vbroker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.entities.Queue;
import com.flipkart.vbroker.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@AllArgsConstructor
public class QueueServiceImpl {

    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();
    private final VBrokerConfig config;
    private final CuratorService curatorService;

    public void createQueue(Queue queue) {

    }

    public Queue getQueue(short queueId) {
        Queue queue = null;
        try {
            queue = curatorService.getData(config.getQueuesPath() + "/" + queueId).handle((data, exception) -> {

                try {
                    return MAPPER.readValue(data, Queue.class);
                } catch (IOException e) {
                    log.error("Error while de-serializing data to Queue.");
                    return null;
                }
            }).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while fetching all queues from zk");
        }
        return queue;
    }

    public List<Queue> getQueues() {
        List<Queue> queues = new ArrayList<>();
        try {
            List<String> queueIds = curatorService.getChildren(config.getQueuesPath()).handle((data, exception) -> {
                if (exception != null) {
                    log.error("Error while fetching all queueIds from zk");
                    return Collections.<String>emptyList();
                }
                return data;
            }).toCompletableFuture().get();
            for (String id : queueIds) {
                queues.add(this.getQueue(Short.valueOf(id)));
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while fetching all queues from zk");
        }
        return queues;

    }
}
