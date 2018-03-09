package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Subscription;
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
import static org.testng.Assert.assertNotNull;

public class SubscriptionServiceImplTest {

	SubscriptionService subscriptionService;
	@Mock
	TopicService topicService;
	@Mock
	CuratorService curatorService;
	@Mock
	TopicPartDataManager topicPartDataManager;
	@Mock
	SubPartDataManager subPartDataManager;
	VBrokerConfig config;

	@BeforeClass
	public void init() throws Exception {

		MockitoAnnotations.initMocks(this);
		config = VBrokerConfig.newConfig("broker.properties");
		subscriptionService = new SubscriptionServiceImpl(config, curatorService, topicPartDataManager,
				subPartDataManager, topicService);
	}

	@Test
	public void shouldCreateSubscription() throws Exception {
		Subscription subscription = DummyEntities.groupedSubscription;
		CompletionStage<String> value = CompletableFuture.supplyAsync(() -> {
			return "subscriptions/0001";
		});
		when(curatorService.createNodeAndSetData(any(String.class), any(CreateMode.class), any(), any()))
				.thenReturn(value);
		subscriptionService.createSubscription(subscription).handle((data, exception) -> {
			assertEquals(data.elastic(), subscription.elastic());
			assertEquals(data.httpMethod(), subscription.httpMethod());
			return null;
		}).toCompletableFuture().get();
	}

	@Test
	public void shouldGetAllSubscriptions() throws Exception {
		CompletionStage<List<Topic>> value = CompletableFuture.supplyAsync(() -> {
			List<Topic> topics = new ArrayList<Topic>();
			topics.add(DummyEntities.groupedTopic);
			return topics;
		});
		when(topicService.getAllTopics()).thenReturn(value);

		String subsPath = config.getTopicsPath() + "/" + DummyEntities.groupedTopic.id() + "/subscriptions";
		CompletionStage<List<String>> subscriptionIds = CompletableFuture.supplyAsync(() -> {
			List<String> subIds = new ArrayList<>();
			subIds.add(String.valueOf(DummyEntities.groupedSubscription.id()));
			return subIds;
		});
		when(curatorService.getChildren(subsPath)).thenReturn(subscriptionIds);

		String subPath = config.getTopicsPath() + "/" + DummyEntities.groupedTopic.id() + "/subscriptions/"
				+ DummyEntities.groupedSubscription.id();
		CompletionStage<byte[]> subscription = CompletableFuture.supplyAsync(() -> {
			return DummyEntities.groupedSubscription.toBytes();
		});
		when(curatorService.getData(subPath)).thenReturn(subscription);

		subscriptionService.getAllSubscriptions().handle((data, exception) -> {
			assertEquals(data.get(0).name(), DummyEntities.groupedSubscription.name());
			return null;
		}).toCompletableFuture().get();
	}

	@Test
	public void shouldGetSubscription() throws Exception {
		String subscriptionPath = config.getTopicsPath() + "/" + DummyEntities.groupedTopic.id() + "/subscriptions/"
				+ DummyEntities.groupedSubscription.id();
		CompletionStage<byte[]> subValue = CompletableFuture.supplyAsync(() -> {
			return DummyEntities.groupedSubscription.toBytes();
		});
		when(curatorService.getData(subscriptionPath)).thenReturn(subValue);
		subscriptionService.getSubscription(DummyEntities.groupedTopic.id(), DummyEntities.groupedSubscription.id())
				.handle((data, exception) -> {
					assertEquals(data.name(), DummyEntities.groupedSubscription.name());
					return null;
				}).toCompletableFuture().get();

	}

	@Test
	public void shouldGetSubscriptionsForTopic() throws Exception {
		int topicId = DummyEntities.groupedTopic.id();
		int subId = DummyEntities.groupedSubscription.id();
		String subsPath = config.getTopicsPath() + "/" + topicId + "/subscriptions";
		CompletionStage<List<String>> subIds = CompletableFuture.supplyAsync(() -> {
			List<String> ids = new ArrayList<>();
			ids.add(String.valueOf(subId));
			return ids;
		});
		when(curatorService.getChildren(subsPath)).thenReturn(subIds);
		subscriptionService.getSubscriptionsForTopic(topicId).handle((data, exception) -> {
			assertEquals(data.size(), 1);
			assertEquals(data.get(0).name(), DummyEntities.groupedSubscription.name());
			return null;
		}).toCompletableFuture().get();
	}

	@Test
	public void shouldGetPartSubscription() throws Exception {
		Subscription subscription = DummyEntities.groupedSubscription;
		int partSubscriptionId = 0;

		String subscriptionPath = config.getTopicsPath() + "/" + DummyEntities.groupedTopic.id() + "/subscriptions/"
				+ DummyEntities.groupedSubscription.id();
		CompletionStage<byte[]> subValue = CompletableFuture.supplyAsync(() -> {
			return DummyEntities.groupedSubscription.toBytes();
		});
		when(curatorService.getData(subscriptionPath)).thenReturn(subValue);

		subscriptionService.getPartSubscription(subscription, partSubscriptionId).handle((data, exception) -> {
			assertEquals(data.getSubscriptionId(), DummyEntities.groupedSubscription.id());
			assertEquals(data.getTopicPartition().getTopicId(), DummyEntities.groupedSubscription.topicId());
			return null;
		}).toCompletableFuture().get();
	}

	@Test
	public void shouldFail_PartSubscription() throws Exception {
		Subscription subscription = DummyEntities.groupedSubscription;
		int partSubscriptionId = 0;

		String subscriptionPath = config.getTopicsPath() + "/" + DummyEntities.groupedTopic.id() + "/subscriptions/"
				+ DummyEntities.groupedSubscription.id();
		CompletionStage<byte[]> subValue = CompletableFuture.supplyAsync(() -> {
			throw new VBrokerException("unknown error");
		});
		when(curatorService.getData(subscriptionPath)).thenReturn(subValue);

		subscriptionService.getPartSubscription(subscription, partSubscriptionId).handle((data, exception) -> {
			assertNotNull(exception);
			return null;
		}).toCompletableFuture().get();
	}

	@Test
	public void shouldGetPartSubscriber() throws Exception {
		Subscription subscription = DummyEntities.groupedSubscription;
		int topicId = DummyEntities.groupedTopic.id();
		int subscriptionId = DummyEntities.groupedSubscription.id();
		boolean grouped = true;

		String subscriptionPath = config.getTopicsPath() + "/" + topicId + "/subscriptions/" + subscriptionId;
		CompletionStage<byte[]> subValue = CompletableFuture.supplyAsync(() -> {
			return DummyEntities.groupedSubscription.toBytes();
		});
		when(curatorService.getData(subscriptionPath)).thenReturn(subValue);

		TopicPartition topicPartition = new TopicPartition(0, topicId, grouped);
		PartSubscription partSubscription = new PartSubscription(0, topicPartition, subscriptionId, grouped);
		subscriptionService.getPartSubscriber(partSubscription).handle((data, exception) -> {
			assertEquals(data.getPartSubscription().getSubscriptionId(), subscriptionId);
			assertEquals(data.getPartSubscription().getId(), 0);
			return null;
		}).toCompletableFuture().get();
	}

}
