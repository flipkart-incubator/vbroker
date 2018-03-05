package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.exceptions.TopicNotFoundException;
import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.GetTopicsRequest;
import com.flipkart.vbroker.proto.GetTopicsResponse;
import com.flipkart.vbroker.proto.ProtoRequest;
import com.flipkart.vbroker.proto.ProtoTopic;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Topic;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by kaushal.hooda on 05/03/18.
 */
public class GetTopicsRequestHandlerTest {
    private GetTopicsRequestHandler getTopicsRequestHandler;
    private TopicService topicService;

    @BeforeMethod
    public void setUp() throws Exception {
        topicService = mock(TopicService.class);
        getTopicsRequestHandler = new GetTopicsRequestHandler(topicService);
    }

    @Test
    public void shouldReturnTopic(){
        int topicId = 1;
        String topicName = "topic_1";
        Topic topic = new Topic(ProtoTopic.newBuilder().setId(topicId).setName(topicName).build());
        when(topicService.getTopic(topicId)).thenReturn(CompletableFuture.completedFuture(topic));

        VRequest vRequest = generateVRequest(Collections.singletonList(topicId));
        VResponse vResponse = getTopicsRequestHandler.handle(vRequest).toCompletableFuture().join();
        GetTopicsResponse response = FlatbufUtils.getProtoResponse(vResponse).getGetTopicsResponse();

        Assert.assertEquals(response.getTopics(0).getTopic().toByteArray(), topic.toBytes());
    }

    @Test
    public void shouldHandleOneExistingAndOneMissingTopic(){
        int topicId = 1;
        String topicName = "topic_1";
        int topicId_2 = 2;
        Topic topic = new Topic(ProtoTopic.newBuilder().setId(topicId).setName(topicName).build());
        when(topicService.getTopic(topicId)).thenReturn(CompletableFuture.completedFuture(topic));
        when(topicService.getTopic(topicId_2)).thenReturn(CompletableFuture.supplyAsync(() -> {
            throw new TopicNotFoundException("Topic not found");
        }));

        VRequest vRequest = generateVRequest(Arrays.asList(topicId, topicId_2));
        VResponse vResponse = getTopicsRequestHandler.handle(vRequest).toCompletableFuture().join();
        GetTopicsResponse response = FlatbufUtils.getProtoResponse(vResponse).getGetTopicsResponse();

        Assert.assertEquals(response.getTopics(0).getTopic().toByteArray(), topic.toBytes());
        Assert.assertEquals(response.getTopics(1).getTopic().getId(), topicId_2);
        Assert.assertEquals(response.getTopics(1).getStatus().getStatusCode(), StatusCode.Failure);
    }

    private VRequest generateVRequest(List<Integer> topics) {
        GetTopicsRequest getTopicsRequest = GetTopicsRequest.newBuilder().addAllIds(topics).build();
        ProtoRequest protoRequest = ProtoRequest.newBuilder().setGetTopicsRequest(getTopicsRequest).build();
        return FlatbufUtils.createVRequest((byte) 1, 1001, protoRequest);
    }

}