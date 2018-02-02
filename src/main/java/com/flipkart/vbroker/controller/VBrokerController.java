package com.flipkart.vbroker.controller;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.curator.x.async.AsyncEventException;
import org.apache.zookeeper.WatchedEvent;

import com.flipkart.vbroker.services.CuratorService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VBrokerController {
	private String path = "/topics";
	private CuratorService curatorService;
	private Executor executor = Executors.newSingleThreadExecutor();

	public VBrokerController(CuratorService curatorService) {
		super();
		this.curatorService = curatorService;
	}

	public void watch() {

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
