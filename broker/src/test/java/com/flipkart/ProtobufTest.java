package com.flipkart;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.flipkart.TestProtos;

import java.util.Arrays;

import static com.flipkart.TestProtos.*;

/**
 * Created by kaushal.hooda on 28/02/18.
 */
@Slf4j
public class ProtobufTest {

    @Test
    public void shouldEncodeDecodeSimpleObject(){
        Topic topic = getSampleTopic(1);
        log.info(topic.toString());
        byte[] topicBytes = topic.toByteArray();
        try {
            Topic topic1 = Topic.parseFrom(topicBytes);
            Assert.assertEquals(topic.getId(), topic1.getId());
            Assert.assertEquals(topic.toByteArray(), topic1.toByteArray());
        } catch (InvalidProtocolBufferException ignored) {
        }
    }

    @Test
    public void shouldConvertToJsonAndBack() throws InvalidProtocolBufferException {
        Topic topic = getSampleTopic(1);
        String topicJson = JsonFormat.printer().print(topic);

        log.info(topicJson);

        Topic.Builder topicBuilder = Topic.newBuilder();
        JsonFormat.parser().merge(topicJson, topicBuilder);
        Topic topic1 = topicBuilder.build();

        Assert.assertEquals(topic.getId(), topic1.getId());
        Assert.assertEquals(topic.toByteArray(), topic1.toByteArray());
        Assert.assertTrue(topic.equals(topic1));
    }

    @Test
    public void shouldConvertToJsonAndBackForCompositeObject() throws InvalidProtocolBufferException {
        GetTopicsRequest getTopicsRequest = GetTopicsRequest.newBuilder().addTopicIds(1).addTopicIds(2).build();
        RequestMessage requestMessage = RequestMessage.newBuilder().setGetTopicsRequest(getTopicsRequest).build();
        VRequest vRequest = VRequest.newBuilder().setRequestMessage(requestMessage).setCorrelationId("abc").build();

        String jsonVRequest = JsonFormat.printer().print(vRequest);
        log.info(jsonVRequest);

        VRequest.Builder builder = VRequest.newBuilder();
        JsonFormat.parser().merge(jsonVRequest, builder);
        VRequest vRequest1 = builder.build();
        Assert.assertEquals(vRequest, vRequest1);
    }

    //composite types
    @Test
    public void shouldHandleCompositeObjects() throws InvalidProtocolBufferException {
        GetTopicsRequest getTopicsRequest = GetTopicsRequest.newBuilder().addTopicIds(1).addTopicIds(2).build();
        RequestMessage requestMessage = RequestMessage.newBuilder().setGetTopicsRequest(getTopicsRequest).build();
        VRequest vRequest = VRequest.newBuilder().setRequestMessage(requestMessage).setCorrelationId("abc").build();

        byte[] requestBytes = vRequest.toByteArray();

        VRequest vRequest1 = VRequest.parseFrom(requestBytes);
        RequestMessage requestMessage1 = vRequest1.getRequestMessage();
        RequestMessage.RequestMessageCase requestMessageCase = requestMessage1.getRequestMessageCase();
        switch (requestMessageCase){
            case GETSUBSCRIPTIONSREQUEST:
                throw new IllegalArgumentException();
            case GETTOPICSREQUEST:
                Assert.assertTrue(requestMessage.getGetTopicsRequest().getTopicIdsList().equals(Arrays.asList(1,2)));
                break;
            case REQUESTMESSAGE_NOT_SET:
                throw new IllegalArgumentException();
        }
    }

    //reuse object
    @Test
    public void shouldBeAbleToReuseObjectsAndRepeatedTypes(){
        Topic topic = getSampleTopic(1);
        VStatus.Builder statusBuilder = VStatus.newBuilder();
        VStatus vStatus = statusBuilder.setStatusCode(200).setMessage("OK").build();

        Topic topic1 = getSampleTopic(2);
//        VStatus.Builder statusBuilder1 = VStatus.newBuilder();
        VStatus vStatus1 = statusBuilder.setStatusCode(200).setMessage("OK").build();

        GetTopicResponse.Builder builder = GetTopicResponse.newBuilder();
        GetTopicResponse getTopicResponse = builder.setStatus(vStatus).setTopic(topic).build();

//        GetTopicResponse.Builder builder1 = GetTopicResponse.newBuilder();
        GetTopicResponse getTopicResponse1 = builder.setStatus(vStatus1).setTopic(topic1).build();

        GetTopicsResponse.Builder finalBuilder = GetTopicsResponse.newBuilder();
        GetTopicsResponse getTopicsResponse = finalBuilder.addResponses(getTopicResponse).addResponses(getTopicResponse1).build();
        Assert.assertEquals(getTopicsResponse.getResponsesCount(), 2);
        Assert.assertEquals(getTopicsResponse.getResponses(0).getTopic().getId(), topic.getId());
    }

    @Test
    public void buildObjectShouldBeFinal(){
        Topic topic = getSampleTopic(1);
        //? No setters in topic, only getters
    }

    private Topic getSampleTopic(int id){
        Topic.Builder topicBuilder = Topic.newBuilder();
        topicBuilder.setId(id);
        topicBuilder.setName("topic-1");
        topicBuilder.setGrouped(true);
        topicBuilder.setPartitions(5);
        topicBuilder.setReplicationFactor(3);
        topicBuilder.setTopicCategory(TopicCategory.TOPIC);
        return topicBuilder.build();
    }

}
