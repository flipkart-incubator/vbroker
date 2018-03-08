package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.data.TopicPartDataManager;
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
            List<Topic> topics = new ArrayList();
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
}
