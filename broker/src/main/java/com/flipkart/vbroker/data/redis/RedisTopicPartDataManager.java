package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartMessage;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.exceptions.NotImplementedException;
import com.google.common.collect.PeekingIterator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Slf4j
public class RedisTopicPartDataManager implements TopicPartDataManager {

    private static final Config config = new Config();
    private static RedissonClient client;
    private static Codec redisCodec = new RedisMessageCodec();
    private final Map<TopicPartition, TopicPartData> allPartitionsDataMap = new LinkedHashMap<>();

    public RedisTopicPartDataManager(VBrokerConfig brokerConfig, EventLoopGroup workerGroup) {
        config.useSingleServer().setAddress(brokerConfig.getRedisUrl());
        config.setCodec(redisCodec);
        config.setEventLoopGroup(workerGroup);
        this.client = Redisson.create(config);
    }

    @Override
    public CompletionStage<TopicPartData> getTopicPartData(TopicPartition topicPartition) {
        return CompletableFuture.supplyAsync(() -> {
            allPartitionsDataMap.putIfAbsent(topicPartition, new RedisTopicPartData(client, topicPartition));
            return allPartitionsDataMap.get(topicPartition);
        });
    }

    @Override
    public CompletionStage<MessageMetadata> addMessage(TopicPartition topicPartition, Message message) {
        return getTopicPartData(topicPartition)
            .thenCompose(topicPartData -> topicPartData.addMessage(message));
    }

    @Override
    public CompletionStage<List<MessageMetadata>> addMessages(List<TopicPartMessage> topicPartMessages) {
        return CompletableFuture.supplyAsync(() -> topicPartMessages.stream()
            .map(topicPartMessage -> getTopicPartData(topicPartMessage.getTopicPartition())
                .thenCompose(topicPartData -> topicPartData.addMessage(topicPartMessage.getMessage()))
                .toCompletableFuture().join() //TODO: fix this
            ).collect(Collectors.toList()));
    }

    @Override
    public CompletionStage<MessageMetadata> addMessageGroup(TopicPartition topicPartition, MessageGroup messageGroup) {
        throw new NotImplementedException("adding messageGroup to topicPartition is not yet implemented");
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups(TopicPartition topicPartition) {
        return getTopicPartData(topicPartition)
            .thenCompose(TopicPartData::getUniqueGroups);
    }

    @Override
    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group) {
        throw new NotImplementedException("Not yet implemented");
    }

    @Override
    public PeekingIterator<Message> getIterator(TopicPartition topicPartition, String group, int seqNoFrom) {
        //TODO: change this to make it non blocking
        return getTopicPartData(topicPartition)
            .thenApplyAsync(topicPartData -> topicPartData.iteratorFrom(group, seqNoFrom))
            .toCompletableFuture().join();
    }
}