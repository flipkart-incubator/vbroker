package com.flipkart.vbroker.curator;

import com.flipkart.vbroker.controller.CuratorService;
import org.apache.zookeeper.WatchedEvent;
import org.testng.annotations.Test;

import java.util.concurrent.CompletionStage;

public class WatchTest {

    @Test
    public void testWatch() throws Exception {
        CuratorService curatorService = new CuratorService();
        CompletionStage<WatchedEvent> s = curatorService.watchNode("/topics");
        handleWatchedStage(s);
    }

    private static void handleWatchedStage(CompletionStage<WatchedEvent> watchedStage) throws Exception {
        watchedStage.thenAccept(event -> {
            System.out.println(event.getType());
            System.out.println(event);
        }).toCompletableFuture().get();

    }
}
