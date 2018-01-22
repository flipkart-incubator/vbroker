package com.flipkart.vbroker.controller;

import com.flipkart.vbroker.core.Topic;
import org.apache.zookeeper.CreateMode;

public class TopicService {

    private static final String topicsPath = "/topics";

    private CuratorService curatorService;

    public TopicService(CuratorService curatorService) {
        super();
        this.curatorService = curatorService;
    }

    public void createTopic(String topicName, String team, Boolean grouped, int replicationFactor, int noOfPartitions,
                            String topicType, String topicCategory) {
        Topic topic = new Topic(team, topicName, grouped, noOfPartitions, replicationFactor);
        curatorService.createNodeAndSetData(topicsPath + "/" + topicName, CreateMode.PERSISTENT,
                topic.toString().getBytes()).handle((data, exception) -> {
            if (exception != null) {
                System.out.println("Failure in creating topic!");
            }
            return null;
        });
    }
}