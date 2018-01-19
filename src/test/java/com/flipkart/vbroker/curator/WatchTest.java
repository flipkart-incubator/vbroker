package com.flipkart.vbroker.curator;

import java.util.concurrent.CompletionStage;

import org.apache.zookeeper.WatchedEvent;
import org.testng.annotations.Test;

import com.flipkart.vbroker.controller.CuratorService;

public class WatchTest {

	@Test
	public void testWatch() throws Exception {
		CuratorService curatorService = new CuratorService();
		CompletionStage<WatchedEvent> s = curatorService.watchNode("/topics");
		handleWatchedStage(s);
	}

	private static void handleWatchedStage(CompletionStage<WatchedEvent> watchedStage) throws Exception {
		// async handling of Watchers is complicated because watchers can
		// trigger multiple times
		// and CompletionStage don't support this behavior

		// thenAccept() handles normal watcher triggering.
		watchedStage.thenAccept(event -> {
			System.out.println(event.getType());
			System.out.println(event);
			// etc.
		}).toCompletableFuture().get();

	}
}
