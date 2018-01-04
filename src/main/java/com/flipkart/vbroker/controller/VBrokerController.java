package com.flipkart.vbroker.controller;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.exceptions.VBrokerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.api.CreateOption;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.CompletionStage;

@Slf4j
public class VBrokerController {

    public static void main() throws IOException {

        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");

        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(
                config.getZookeeperUrl(),
                new ExponentialBackoffRetry(1000, 5));

        String path = "/vbroker/test";

        AsyncCuratorFramework asyncZkClient = AsyncCuratorFramework.wrap(curatorFramework);
        CompletionStage<Object> handle = asyncZkClient.create()
                .withOptions(EnumSet.of(CreateOption.doProtected))
                .forPath(path)
                .handle((actualPath, exception) -> {
                    if (exception != null) {
                        log.error("Exception in creating node");
                        throw new VBrokerException("Exception in creating node");
                    }
                    return null;
                });

    }
}
