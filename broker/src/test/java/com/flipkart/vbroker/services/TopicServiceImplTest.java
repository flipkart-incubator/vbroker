package com.flipkart.vbroker.services;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.exceptions.TopicCreationException;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.utils.DummyEntities;

import org.apache.zookeeper.CreateMode;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        when(curatorService.createNodeAndSetData(any(String.class), any(CreateMode.class), any())).thenReturn(value);
        Topic topic1 = DummyEntities.groupedTopic;
        topicService.createTopic(topic1).handle((data, exception) -> {
            assertEquals(data.id(), 0001);
            assertEquals(topic1.name(), data.name());
            assertEquals(topic1.grouped(), data.grouped());
            return null;
        }).toCompletableFuture().get();
    }

    @Test
    public void shouldCreateTopic_AndFail_ForWrongId() throws Exception {
        CompletionStage<String> value = CompletableFuture.supplyAsync(() -> {
            return "0001";
        });
        when(curatorService.createNodeAndSetData(any(String.class), any(CreateMode.class), any())).thenReturn(value);
        Topic topic1 = DummyEntities.groupedTopic;
        topicService.createTopic(topic1).handle((data, exception) -> {
            assertTrue(exception.getCause() instanceof ArrayIndexOutOfBoundsException);
            return null;
        }).toCompletableFuture().get();
    }

    @Test
    public void shouldCreateTopic_AndFail_ForCuratorException() throws Exception {
        CompletionStage<String> value = CompletableFuture.supplyAsync(() -> {
            throw new VBrokerException("Unknown error");
        });
        when(curatorService.createNodeAndSetData(any(String.class), any(CreateMode.class), any()))
            .thenReturn(value);
        Topic topic1 = DummyEntities.groupedTopic;
        topicService.createTopic(topic1).handle((data, exception) -> {
            assertTrue(exception.getCause() instanceof TopicCreationException);
            return null;
        }).toCompletableFuture().get();
    }
}
