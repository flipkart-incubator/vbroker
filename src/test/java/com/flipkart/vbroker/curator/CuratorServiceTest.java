package com.flipkart.vbroker.curator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.curator.x.async.AsyncStage;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.services.CuratorService;

public class CuratorServiceTest {

	CuratorService service;
	String path;
	String data;

	@BeforeClass
	public void init() throws Exception {
		service = new CuratorService();
		service.init();
		path = "/test/test";
		data = "test message";
	}

	@Test
	public void testSetAndGetData() throws TimeoutException, ExecutionException, InterruptedException {

		AsyncStage<String> createStage = service.createNode(path, CreateMode.PERSISTENT);
		createStage.toCompletableFuture().get(20, TimeUnit.SECONDS);
		AsyncStage<Stat> setStage = service.setData(path, data.getBytes());
		setStage.toCompletableFuture().get(20, TimeUnit.SECONDS);
		AsyncStage<byte[]> getStage = service.getData(path);
		getStage.handle((actualData, exception) -> {
			if (exception != null) {
				throw new VBrokerException("Exception in getting data");
			}
			Assert.assertEquals(actualData, data.getBytes());
			return null;
		}).toCompletableFuture().get(20, TimeUnit.SECONDS);
	}
}