package com.flipkart.vbroker.curator;

import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.services.TopicServiceImpl;
import com.flipkart.vbroker.wrappers.Topic;
import org.testng.annotations.Test;

import static java.util.Objects.nonNull;

public class GetTopicTest {

    @Test
    public void getTopicTest() throws Exception {
        CuratorService curatorService = new CuratorService(null);
        TopicService topicService = new TopicServiceImpl(null, curatorService);
        Topic topic = topicService.getTopic((short) 1).toCompletableFuture().join();
        assert nonNull(topic);
    }
}
