package com.flipkart.vbroker.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.AsyncStage;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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
        String createdNode = curatorService.createNodeAndSetData(path, CreateMode.PERSISTENT, data.getBytes())
            .toCompletableFuture().join();
        assertEquals(createdNode, path);

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
}
