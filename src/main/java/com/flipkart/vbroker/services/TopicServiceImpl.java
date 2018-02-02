package com.flipkart.vbroker.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.exceptions.VBrokerException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.zookeeper.CreateMode;

public class TopicServiceImpl implements TopicService {

	 private static final String topicsPath = "/topics";
	 private final CuratorService curatorService;

    private final ConcurrentMap<Short, Topic> topicsMap = new ConcurrentHashMap<>();

    public TopicServiceImpl(CuratorService curatorService) {
        super();
        this.curatorService = curatorService;
    }
    
    @Override
    public synchronized void createTopic(Topic topic){
        topicsMap.putIfAbsent(topic.getId(), topic);
        try{
        curatorService.createNodeAndSetData(topicsPath + "/" + topic.getName(), CreateMode.PERSISTENT,
                topic.toJson().getBytes()).handle((data, exception) -> {
            if (exception != null) {
                System.out.println("Failure in creating topic!");
            }
            return null;
        });
        } catch (JsonProcessingException je){
        	throw new VBrokerException("Json serialization failed for topic with id " + topic.getId());
        }
    }

    @Override
    public synchronized void createTopicPartition(Topic topic, TopicPartition topicPartition) {
        topic.addPartition(topicPartition);
        topicsMap.putIfAbsent(topic.getId(), topic);
    }

    @Override
    public TopicPartition getTopicPartition(Topic topic, short topicPartitionId) {
        if (!topicsMap.containsKey(topic.getId())) {
            throw new VBrokerException("Not found topic with id: " + topic.getId());
        }
        Topic existingTopic = topicsMap.get(topic.getId());
        return existingTopic.getPartition(topicPartitionId);
    }

    @Override
    public Topic getTopic(short topicId) {
        return topicsMap.get(topicId);
    }

    @Override
    public List<TopicPartition> getPartitions(Topic topic) {
        return topic.getPartitions();
    }
}
