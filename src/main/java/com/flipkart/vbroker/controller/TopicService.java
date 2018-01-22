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

    public void createTopic(Topic topic) {
        curatorService.createNodeAndSetData(topicsPath + "/" + topic.getName(), CreateMode.PERSISTENT,
                topic.toString().getBytes()).handle((data, exception) -> {
            if (exception != null) {
                System.out.println("Failure in creating topic!");
            }
            return null;
        });
    }
}