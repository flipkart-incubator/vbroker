package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.GetAllTopicsRequest;
import com.flipkart.vbroker.proto.GetAllTopicsResponse;
import com.flipkart.vbroker.proto.ProtoRequest;
import com.flipkart.vbroker.proto.ProtoTopic;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Topic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Created by kaushal.hooda on 06/03/18.
 */
public class GetAllTopicsRequestHandlerTest {
    private GetAllTopicsRequestHandler getAllTopicsRequestHandler;
    private TopicService topicService;

    @BeforeMethod
    public void setUp() throws Exception {
        topicService = mock(TopicService.class);
        getAllTopicsRequestHandler = new GetAllTopicsRequestHandler(topicService);
    }

    @Test
    public void shouldHandleHappyCase() {
        Topic topic = new Topic(ProtoTopic.newBuilder().setId(1).setName("topic_1").build());
        when(topicService.getAllTopics())
            .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(topic)));

        GetAllTopicsResponse response = makeRequest();

        assertEquals(response.getTopicsCount(), 1);
        assertEquals(response.getTopics(0).toByteArray(), topic.toBytes());
    }

    @Test
    public void shouldHandleException() {
        when(topicService.getAllTopics())
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Error in fetching topics");
            }));

        GetAllTopicsResponse response = makeRequest();

        assertEquals(response.getTopicsCount(), 0);
        assertEquals(response.getStatus().getStatusCode(), StatusCode.Failure);
    }

    private GetAllTopicsResponse makeRequest() {
        VRequest vRequest = generateVRequest();
        VResponse vResponse = getAllTopicsRequestHandler.handle(vRequest).toCompletableFuture().join();
        return FlatbufUtils.getProtoResponse(vResponse).getGetAllTopicsResponse();
    }

    private VRequest generateVRequest() {
        ProtoRequest protoRequest = ProtoRequest.newBuilder().setGetAllTopicsRequest(GetAllTopicsRequest.getDefaultInstance()).build();
        return FlatbufUtils.createVRequest((byte) 1, 1001, protoRequest);
    }

}