package com.flipkart.vbroker.curator;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.services.TopicServiceImpl;
import org.testng.annotations.Test;

public class GetTopicTest {

    @Test
    public void getTopicTest() throws Exception {
        CuratorService curatorService = new CuratorService(VBrokerConfig.newConfig("broker.properties"));
        TopicService topicService = new TopicServiceImpl(VBrokerConfig.newConfig("broker.properties"), curatorService);
        Topic topic = topicService.getTopic((short) 201);
        System.out.println(topic.topicId());
    }
}
