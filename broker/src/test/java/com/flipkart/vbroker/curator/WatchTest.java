package com.flipkart.vbroker.curator;

import com.flipkart.vbroker.services.CuratorService;
import org.apache.zookeeper.WatchedEvent;
import org.testng.annotations.Test;

import java.util.concurrent.CompletionStage;

public class WatchTest {

    private static void handleWatchedStage(CompletionStage<WatchedEvent> watchedStage) throws Exception {
        watchedStage.thenAccept(event -> {
            System.out.println(event.getType());
            System.out.println(event);
        }).toCompletableFuture().get();

    }

    @Test
    public void testWatch() throws Exception {
        CuratorService curatorService = new CuratorService(null);
        CompletionStage<WatchedEvent> s = curatorService.watchNode("/topics");
        handleWatchedStage(s);
    }
}