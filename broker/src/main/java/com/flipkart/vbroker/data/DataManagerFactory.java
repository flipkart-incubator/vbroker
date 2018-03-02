package com.flipkart.vbroker.data;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.data.memory.InMemorySubPartDataManager;
import com.flipkart.vbroker.data.memory.InMemoryTopicPartDataManager;
import com.flipkart.vbroker.data.redis.RedisMessageCodec;
import com.flipkart.vbroker.data.redis.RedisTopicPartDataManager;
import com.flipkart.vbroker.exceptions.VBrokerException;
import io.netty.channel.EventLoopGroup;
import lombok.AllArgsConstructor;
import org.redisson.Redisson;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;

@AllArgsConstructor
public class DataManagerFactory {

    private final VBrokerConfig config;
    private EventLoopGroup workerGroup;

    public TopicPartDataManager getTopicDataManager() {
        TopicPartDataManager topicPartDataManager;
        switch (config.getTopicDataManager()) {
            case redis:
                Config redissonConfig = getRedissonConfig();
                topicPartDataManager = new RedisTopicPartDataManager(Redisson.create(redissonConfig));
                break;
            case in_memory:
                topicPartDataManager = new InMemoryTopicPartDataManager();
                break;
            default:
                throw new VBrokerException("Unknown dataManager: " + config.getTopicDataManager());
        }

        return topicPartDataManager;
    }

    public SubPartDataManager getSubPartDataManager(TopicPartDataManager topicPartDataManager) {
        SubPartDataManager subPartDataManager;
        switch (config.getSubscriptionDataManager()) {
            case in_memory:
                subPartDataManager = new InMemorySubPartDataManager(topicPartDataManager);
                break;
            //TODO: fill this when redisSubPartDataManager is implemented
            default:
                throw new VBrokerException("Unknown dataManager: " + config.getSubscriptionDataManager());
        }
        return subPartDataManager;
    }

    private Config getRedissonConfig() {
        Config redissonConfig = new Config();
        if (config.isRedisCluster() && config.getRedisClusterNodes() != null) {
            ClusterServersConfig clusterServersConfig = redissonConfig.useClusterServers()
                .setScanInterval(2000);
            for (String node : config.getRedisClusterNodes()) {
                clusterServersConfig.addNodeAddress(node);
            }
        } else if (!config.isRedisCluster() && config.getRedisUrl() != null) {
            redissonConfig.useSingleServer().setAddress(config.getRedisUrl());
        }

        redissonConfig.setCodec(new RedisMessageCodec());
        redissonConfig.setEventLoopGroup(workerGroup);
        return redissonConfig;
    }
}
