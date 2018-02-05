package com.flipkart.vbroker.services;

import static java.util.EnumSet.of;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.AsyncStage;
import org.apache.curator.x.async.api.CreateOption;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

import com.flipkart.vbroker.VBrokerConfig;

public class CuratorService {

	private AsyncCuratorFramework asyncZkClient;

	public CuratorService() throws IOException {
		super();
		init();
	}

	/**
	 * Initializes the async client.
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {

		VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
		CuratorFramework client = CuratorFrameworkFactory.newClient(config.getZookeeperUrl(),
				new ExponentialBackoffRetry(1000, 5));
		client.start();
		asyncZkClient = AsyncCuratorFramework.wrap(client);
	}

	/**
	 * Creates node as per path.
	 * 
	 * @param path
	 * @param createMode
	 * @return
	 */
	public AsyncStage<String> createNode(String path, CreateMode createMode) {
		return asyncZkClient.create()
				.withOptions(of(CreateOption.setDataIfExists, CreateOption.createParentsIfNeeded), createMode)
				.forPath(path);
	}

	/**
	 * Creates node at path and set data.
	 * 
	 * @param path
	 * @param createMode
	 * @param data
	 * @return
	 */
	public AsyncStage<String> createNodeAndSetData(String path, CreateMode createMode, byte[] data) {
		return asyncZkClient.create()
				.withOptions(of(CreateOption.setDataIfExists, CreateOption.createParentsIfNeeded), createMode)
				.forPath(path, data);
	}

	/**
	 * Sets watch on path and returns the stage for watched event.
	 * 
	 * @param path
	 * @return
	 */
	public CompletionStage<WatchedEvent> watchNode(String path) {
		return asyncZkClient.watched().checkExists().forPath(path).event();
	}

	/**
	 * Gets data at path.
	 * 
	 * @param path
	 * @return
	 */
	public AsyncStage<byte[]> getData(String path) {
		return asyncZkClient.getData().forPath(path);
	}

	/**
	 * Sets data at path.
	 * 
	 * @param path
	 * @param data
	 * @return
	 */
	public AsyncStage<Stat> setData(String path, byte[] data) {
		return asyncZkClient.setData().forPath(path, data);
	}
}
