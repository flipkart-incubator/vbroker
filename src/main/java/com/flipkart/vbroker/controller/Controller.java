package com.flipkart.vbroker.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.Watcher.Event.EventType;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class Controller {

    private String path = "/vbroker/vbrokers/topics";
    private CuratorService curatorService;

    public Controller(CuratorService curatorService) {
        super();
        this.curatorService = curatorService;
    }

    public void watch() throws Exception {

        Executor executor = Executors.newFixedThreadPool(1);
        curatorService.watchNode(path).handleAsync((data, exception) -> {
            String path = data.getPath();
            EventType type = data.getType();
            if (EventType.NodeCreated.equals(type)) {
                log.info("New topic detected. Run allocation and write to co-ordinator");
            }
            return null;
        }, executor);
    }

}
