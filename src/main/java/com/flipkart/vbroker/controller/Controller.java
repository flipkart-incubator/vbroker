package com.flipkart.vbroker.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.x.async.AsyncEventException;
import org.apache.zookeeper.WatchedEvent;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class Controller {

    private String path = "/topics";
    private CuratorService curatorService;
    private Executor executor = Executors.newSingleThreadExecutor();

    public Controller(CuratorService curatorService) {
        super();
        this.curatorService = curatorService;
    }

    public void watch() throws Exception {

        log.info("Setting watch on topics path...");
        CompletionStage<WatchedEvent> s = curatorService.watchNode(path);
        handleWatchedStage(s);
    }

    private void handleWatchedStage(CompletionStage<WatchedEvent> watchedStage) {
        watchedStage.handleAsync((data, exception) -> {
            if (exception != null) {
                log.error("Exception occured..");
                AsyncEventException asyncEx = (AsyncEventException) exception;
                asyncEx.printStackTrace(); // handle the error as needed
                handleWatchedStage(asyncEx.reset());
            } else {
                log.info(data.getState() + " " + data.getPath() + " " + data.getType());
                try {
                    this.watch();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }, executor);
    }

}
