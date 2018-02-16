package com.flipkart.vbroker.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.AsyncStage;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Slf4j
public class CuratorServiceTest extends BaseClassForTests {

    private CuratorFramework client;
    private CuratorService curatorService;

    @BeforeMethod
    @Override
    public void setup() throws Exception {
        System.setProperty("znode.container.checkIntervalMs", "1000");
        super.setup();
        client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();
        AsyncCuratorFramework asyncClient = AsyncCuratorFramework.wrap(client);
        curatorService = new CuratorService(asyncClient);
    }

    @AfterMethod
    @Override
    public void teardown() throws Exception {
        System.clearProperty("znode.container.checkIntervalMs");
        client.close();
        super.teardown();
    }

    @Test
    public void shouldCreateNode() {
        String path = "/testNode";
        AsyncStage<String> stage = curatorService.createNode(path, CreateMode.PERSISTENT);
        String createdPath = stage.toCompletableFuture().join();
        assertEquals(createdPath, path);
    }

    @Test
    public void shouldCreateNodeAndSetData_GetDataAndValidate() {
        String path = "/testNode_2";
        String data = "ZkNodeDataWithSampleContent";
        createNode(path, data);

        byte[] readBytes = curatorService.getData(path).toCompletableFuture().join();
        assertEquals(readBytes, data.getBytes());
    }

    @Test
    public void shouldSetData_GetSetDataAndValidate() {
        String path = "/testNode_3";
        String data = "ZkNode3WithSampleContent";
        Stat stat = curatorService.createNode(path, CreateMode.PERSISTENT)
            .thenCompose(p -> curatorService.setData(p, data.getBytes()))
            .toCompletableFuture().join();
        assertNotNull(stat);
        assertEquals(stat.getDataLength(), data.getBytes().length);

        byte[] readBytes = curatorService.getData(path).toCompletableFuture().join();
        assertEquals(readBytes, data.getBytes());
    }

    @Test
    public void shouldCreateChildNodesWithParent_AndGetChildNodesForParent() {
        String parentNode = "/parentNode_1";
        List<String> childNodes = Arrays.asList("childNode_1", "childNode_2");
        List<String> createdNodes = childNodes.stream()
            .map(childNode -> curatorService
                .createNode(parentNode + "/" + childNode, CreateMode.PERSISTENT)
                .toCompletableFuture().join())
            .sorted()
            .collect(Collectors.toList());
        assertEquals(createdNodes.size(), childNodes.size());

        List<String> readChildNodes = curatorService.getChildren(parentNode)
            .toCompletableFuture().join()
            .stream().sorted().collect(Collectors.toList());
        assertEquals(readChildNodes, childNodes);
    }

    @Test
    public void shouldSetWatch_AndExecuteActionOnWatchTrigger() throws InterruptedException {
        String parentNode = "/adminNode_1";
        String childNode = "childNode_1";
        String fullChildPath = parentNode + "/" + childNode;
        String childNodeData = "ZkChildNodeWithSampleContent";

        createNode(parentNode, "ZkParentNodeWithSampleContent");

        CountDownLatch watchLatch = new CountDownLatch(1);
        CountDownLatch childLatch = new CountDownLatch(1);

        //1. set watch on parent node first
        curatorService.watchNodeChildren(parentNode)
            .thenAcceptAsync(watchedEvent -> {
                log.info("==Thread: {} ==", Thread.currentThread().getName());
                assertEquals(watchedEvent.getPath(), parentNode);
                assertEquals(watchedEvent.getType(), Watcher.Event.EventType.NodeChildrenChanged);
                watchLatch.countDown();
                log.info("WatchEvent handled successfully");
            }).exceptionally(throwable -> {
            log.error("Exception in watchEvent", throwable);
            return null;
        });

        //2. Create a child node under the parent
        createNode(fullChildPath, childNodeData);

        //3. Await until the watch on getChildren() triggers for the parent node
        log.info("Awaiting on watchLatch as we have created a child node");
        watchLatch.await();

        //4. Get the children in a separate getChildren() call to zk
        curatorService.getChildren(parentNode).thenAcceptAsync(children -> {
            log.info("==Thread: {} ==", Thread.currentThread().getName());
            assertEquals(children.size(), 1);
            assertEquals(children.get(0), childNode);
            childLatch.countDown();
        }).exceptionally(throwable -> {
            log.error("Exception in get children", throwable);
            return null;
        });

        log.info("Awaiting on childLatch");
        childLatch.await();
    }

    private void createNode(String fullChildPath, String childNodeData) {
        String path = curatorService.createNodeAndSetData(fullChildPath, CreateMode.PERSISTENT, childNodeData.getBytes())
            .toCompletableFuture().join();
        assertEquals(path, fullChildPath);
    }
}
