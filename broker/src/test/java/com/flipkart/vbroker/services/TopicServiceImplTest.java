package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.exceptions.TopicCreationException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Topic;
import org.apache.zookeeper.CreateMode;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TopicServiceImplTest {

	TopicService topicService;
	@Mock
	CuratorService curatorService;
	VBrokerConfig config = null;

	@BeforeClass
	public void init() throws Exception {

		MockitoAnnotations.initMocks(this);
		config = VBrokerConfig.newConfig("broker.properties");
		topicService = new TopicServiceImpl(config, curatorService);
	}

	@Test
	public void shouldCreateTopic() throws Exception {
		CompletionStage<String> value = CompletableFuture.supplyAsync(() -> {
			return "topics/0001";
		});
		when(curatorService.createNodeAndSetData(any(String.class), any(CreateMode.class), any(), any()))
				.thenReturn(value);
		Topic topic1 = DummyEntities.groupedTopic;
		topicService.createTopic(topic1).handle((data, exception) -> {
			// assertEquals(data.id(), 0001);
			assertEquals(topic1.name(), data.name());
			assertEquals(topic1.grouped(), data.grouped());
			return null;
		}).toCompletableFuture().get();
	}

	// @Test
	public void shouldCreateTopic_AndFail_ForWrongId() throws Exception {
		CompletionStage<String> value = CompletableFuture.supplyAsync(() -> {
			return "0001";
		});
		when(curatorService.createNodeAndSetData(any(String.class), any(CreateMode.class), any(), any()))
				.thenReturn(value);
		Topic topic1 = DummyEntities.groupedTopic;
		topicService.createTopic(topic1).handle((data, exception) -> {
			assertTrue(exception.getCause() instanceof ArrayIndexOutOfBoundsException);
			return null;
		}).toCompletableFuture().get();
	}

	@Test
	public void shouldFailCreateTopic_ForCuratorException() throws Exception {
		CompletionStage<String> value = CompletableFuture.supplyAsync(() -> {
			throw new VBrokerException("Unknown error");
		});
		when(curatorService.createNodeAndSetData(any(String.class), any(CreateMode.class), any(), any()))
				.thenReturn(value);
		Topic topic1 = DummyEntities.groupedTopic;
		topicService.createTopic(topic1).handle((data, exception) -> {
			assertTrue(exception.getCause() instanceof TopicCreationException);
			return null;
		}).toCompletableFuture().get();
	}

	@Test
	public void shouldGetTopic() throws Exception {
		int topicId = DummyEntities.groupedTopic.id();
		String path = config.getTopicsPath() + "/" + topicId;
		CompletionStage<byte[]> topicBytes = CompletableFuture.supplyAsync(() -> {
			return DummyEntities.groupedTopic.toBytes();
		});
		when(curatorService.getData(path)).thenReturn(topicBytes);
		topicService.getTopic(topicId).handle((data, exception) -> {
			assertEquals(data.topicCategory(), DummyEntities.groupedTopic.topicCategory());
			assertEquals(data.name(), DummyEntities.groupedTopic.name());
			return null;
		});
	}

	@Test
	public void shouldGetAllTopics() throws Exception {
		int topicId1 = DummyEntities.groupedTopic.id();
		int topicId2 = DummyEntities.unGroupedTopic.id();
		CompletionStage<List<String>> topicIds = CompletableFuture.supplyAsync(() -> {
			List<String> ids = new ArrayList();
			ids.add(String.valueOf(topicId1));
			ids.add(String.valueOf(topicId2));
			return ids;
		});
		
		CompletionStage<byte[]> topic1Bytes = CompletableFuture.supplyAsync(() -> {
			return DummyEntities.groupedTopic.toBytes();
		});
		CompletionStage<byte[]> topic2Bytes = CompletableFuture.supplyAsync(() -> {
			return DummyEntities.unGroupedTopic.toBytes();
		});
		when(curatorService.getData(config.getTopicsPath() + "/" + topicId1)).thenReturn(topic1Bytes);
		when(curatorService.getData(config.getTopicsPath() + "/" + topicId2)).thenReturn(topic2Bytes);
		
		when(curatorService.getChildren(config.getTopicsPath())).thenReturn(topicIds);
		topicService.getAllTopics().handle((data, exception) -> {
			assertEquals(data.size(), 2);
			return null;
		});
	}

	@Test
	public void shouldGetTopicPartition() throws Exception {
		Topic topic = DummyEntities.groupedTopic;
		int topicPartitionId = 0;
		int topicId = DummyEntities.groupedTopic.id();
		String path = config.getTopicsPath() + "/" + topicId;
		CompletionStage<byte[]> topicBytes = CompletableFuture.supplyAsync(() -> {
			return DummyEntities.groupedTopic.toBytes();
		});
		when(curatorService.getData(path)).thenReturn(topicBytes);
		topicService.getTopicPartition(topic, topicPartitionId).handle((data, exception) -> {
			assertEquals(data.isGrouped(), DummyEntities.groupedTopic.grouped());
			assertEquals(data.getTopicId(), topicId);
			assertEquals(data.getId(), 0);
			return null;
		});
	}

}
