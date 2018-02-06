package com.flipkart.vbroker.core;

import com.google.common.collect.ForwardingMap;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageGroupMap<K, V> extends ForwardingMap {

    private final static Map<TopicPartitionId, MessageGroupMap> multitonMap = new HashMap<TopicPartitionId, MessageGroupMap>();
    private final static Map<String, Object> kvConcurrentHashMap = new ConcurrentHashMap<String, Object>();
    private short partitionId;
    private short topicId;

    private MessageGroupMap() {
    }

    private MessageGroupMap(short partitionId, short topicId) {
        this.partitionId = partitionId;
        this.topicId = topicId;
    }

    public static MessageGroupMap getInstance(short partitionId, short topicId) {
        TopicPartitionId topicPartitionId = new TopicPartitionId(topicId, partitionId);
        MessageGroupMap instance = multitonMap.get(topicPartitionId);
        if (instance == null) {
            synchronized (MessageGroupMap.class) {
                instance = new MessageGroupMap(partitionId, topicId);
                multitonMap.put(topicPartitionId, instance);
            }
        }
        return instance;
    }

    @Override
    protected Map delegate() {
        return kvConcurrentHashMap;
    }

}
