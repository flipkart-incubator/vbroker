package com.flipkart.vbroker.data.redis;

import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestRedisUtils {
    private static final String redisUrl = "redis://127.0.0.1:6379";
    private static List<String> redisClusterNodes = new ArrayList<>();
    private static RedisServer redisServer;

    static {
        redisClusterNodes.add("redis://127.0.0.1:6379");
        try {
            redisServer = new RedisServer(6379);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRedisUrl() {
        return redisUrl;
    }

    public static List<String> getRedisClusterNodes() {
        return redisClusterNodes;
    }

    public static void startRedisServer() {
        redisServer.start();
    }

    public static void stopRedisServer() {
        redisServer.stop();
    }
}
