package com.flipkart.vbroker.curator;

import org.testng.annotations.Test;

import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.services.CuratorService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.services.TopicServiceImpl;

public class GetTopicTest {

	@Test
	public void getTopicTest() throws Exception {
		CuratorService curatorService = new CuratorService();
		TopicService topicService = new TopicServiceImpl(curatorService);
		Topic topic = topicService.getTopic((short) 1);
		System.out.println(topic.toJson());
	}
}
