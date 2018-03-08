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
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static java.util.EnumSet.of;

@AllArgsConstructor
public class CuratorService {

    private final AsyncCuratorFramework asyncZkClient;

    /**
     * Creates node at path
     *
     * @param path            path to create node at
     * @param createMode      persistent/ephemeral
     * @param setDataIfExists flag to indicate if data is to be set in case node at path
     *                        already exists. If false, throws exception in case the node
     *                        already exists
     * @return
     */
    public AsyncStage<String> createNode(String path, CreateMode createMode, Boolean setDataIfExists) {
        Set<CreateOption> options = of(CreateOption.createParentsIfNeeded);
        if (setDataIfExists)
            options.add(CreateOption.setDataIfExists);
        return asyncZkClient.create().withOptions(options, createMode).forPath(path);
    }

    /**
     * Creates node at path and sets the data
     *
     * @param path            path to create node at
     * @param createMode      persistent/ephemeral
     * @param data            data to be set for node
     * @param setDataIfExists flag to indicate if data is to be set in case node at path
     *                        already exists. If false, throws exception in case the node
     *                        already exists
     * @return
     */
    public CompletionStage<String> createNodeAndSetData(String path, CreateMode createMode, byte[] data,
                                                        Boolean setDataIfExists) {
        Set<CreateOption> options = of(CreateOption.createParentsIfNeeded);
        if (setDataIfExists)
            options.add(CreateOption.setDataIfExists);
        return asyncZkClient.create().withOptions(options, createMode).forPath(path, data);
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
    public CompletionStage<byte[]> getData(String path) {
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
    public CompletionStage<List<String>> getChildren(String path) {
        return asyncZkClient.getChildren().forPath(path);
    }

    public void transaction() {
        CuratorOp p1 = asyncZkClient.transactionOp().create().forPath("p1");
        CuratorOp p2 = asyncZkClient.transactionOp().create().forPath("p2");
        asyncZkClient.transaction();
    }
}
