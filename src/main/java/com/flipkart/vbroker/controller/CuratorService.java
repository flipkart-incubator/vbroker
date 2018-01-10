package com.flipkart.vbroker.controller;

import com.flipkart.vbroker.VBrokerConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.AsyncStage;
import org.apache.curator.x.async.api.CreateOption;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static java.util.EnumSet.of;

public class CuratorService {

    private AsyncCuratorFramework asyncZkClient;

    public CuratorService() throws IOException {
        super();
        init();
    }

    public void init() throws IOException {

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        CuratorFramework client = CuratorFrameworkFactory.newClient(config.getZookeeperUrl(),
                new ExponentialBackoffRetry(1000, 5));
        client.start();
        asyncZkClient = AsyncCuratorFramework.wrap(client);
    }

    public AsyncStage<String> createNode(String path, CreateMode createMode) {
        return asyncZkClient.create()
                .withOptions(of(CreateOption.setDataIfExists, CreateOption.createParentsIfNeeded), createMode)
                .forPath(path);
    }

    public AsyncStage<String> createNodeAndSetData(String path, CreateMode createMode, byte[] data) {
        return asyncZkClient.create()
                .withOptions(of(CreateOption.setDataIfExists, CreateOption.createParentsIfNeeded), createMode)
                .forPath(path, data);
    }

    public CompletionStage<WatchedEvent> watchNode(String path) {
        return asyncZkClient.watched().getData().forPath(path).event();
    }

    public AsyncStage<byte[]> getData(String path) {
        return asyncZkClient.getData().forPath(path);
    }

    public AsyncStage<Stat> setData(String path, byte[] data) {
        return asyncZkClient.setData().forPath(path, data);
    }
}
