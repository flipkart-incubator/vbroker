package com.flipkart.vbroker.services;

import lombok.AllArgsConstructor;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.AsyncStage;
import org.apache.curator.x.async.api.CreateOption;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.EnumSet.of;

@AllArgsConstructor
public class CuratorService {

    private final AsyncCuratorFramework asyncZkClient;

    /**
     * Creates node as per path. If already exists, this method won't throw an
     * exception as it sets data, meaning re-creates.
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
     * Creates node at path and set data. This method wont throw an exception if
     * node already exists, it just sets the new data.
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
     * set watch for the data change on path
     *
     * @param path to set watch on
     * @return the completion stage of watch event
     */
    public CompletionStage<WatchedEvent> watchNodeData(String path) {
        return asyncZkClient.watched().getData().forPath(path).event();
    }

    /**
     * set watch for the child node changes for a parent path
     *
     * @param path to set watch on
     * @return the completion stage of watch event
     */
    public CompletionStage<WatchedEvent> watchNodeChildren(String path) {
        return asyncZkClient.watched().getChildren().forPath(path).event();
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

    /**
     * @param path
     * @return
     */
    public AsyncStage<List<String>> getChildren(String path) {
        return asyncZkClient.getChildren().forPath(path);
    }

    public void transaction() {
        CuratorOp p1 = asyncZkClient.transactionOp().create().forPath("p1");
        CuratorOp p2 = asyncZkClient.transactionOp().create().forPath("p2");
        asyncZkClient.transaction();
    }
}
