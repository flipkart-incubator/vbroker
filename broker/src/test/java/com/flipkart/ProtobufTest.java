package com.flipkart;

import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.wrappers.Topic;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Created by kaushal.hooda on 28/02/18.
 */
@Slf4j
public class ProtobufTest {

    @Test
    public void shouldEncodeDecodeSimpleObject() {
        Topic topic = getSampleTopic(1);
        log.info(topic.toString());
        byte[] topicBytes = topic.toBytes();
        Topic topic1 = Topic.fromBytes(topicBytes);
        Assert.assertEquals(topic.id(), topic1.id());
        Assert.assertEquals(topic.toBytes(), topic1.toBytes());
    }

    @Test
    public void shouldConvertToJsonAndBack() throws InvalidProtocolBufferException {
        Topic topic = getSampleTopic(1);
        String topicJson = topic.toJson();

        log.info(topicJson);

        ProtoTopic.Builder topicBuilder = ProtoTopic.newBuilder();
        JsonFormat.parser().merge(topicJson, topicBuilder);
        Topic topic1 = new Topic(topicBuilder.build());

        Assert.assertEquals(topic.id(), topic1.id());
        Assert.assertEquals(topic.toBytes(), topic1.toBytes());
        Assert.assertTrue(topic.equals(topic1));
    }

    @Test
    public void shouldConvertToJsonAndBackForCompositeObject() throws InvalidProtocolBufferException {
        GetTopicsRequest getTopicsRequest = GetTopicsRequest.newBuilder().addIds(1).addIds(2).build();
        ProtoRequest protoRequest = ProtoRequest.newBuilder().setGetTopicsRequest(getTopicsRequest).build();

        String jsonRequest = JsonFormat.printer().print(protoRequest);
        log.info(jsonRequest);

        ProtoRequest.Builder builder = ProtoRequest.newBuilder();
        JsonFormat.parser().merge(jsonRequest, builder);
        ProtoRequest protoRequest1 = builder.build();
        Assert.assertEquals(protoRequest, protoRequest1);
    }

    //composite types
    @Test
    public void shouldHandleCompositeObjects() throws InvalidProtocolBufferException {
        GetTopicsRequest getTopicsRequest = GetTopicsRequest.newBuilder().addIds(1).addIds(2).build();
        ProtoRequest protoRequest = ProtoRequest.newBuilder().setGetTopicsRequest(getTopicsRequest).build();

        byte[] requestBytes = protoRequest.toByteArray();

        ProtoRequest protoRequest1 = ProtoRequest.parseFrom(requestBytes);
        switch (protoRequest1.getProtoRequestCase()) {
            case GETSUBSCRIPTIONSREQUEST:
                throw new IllegalArgumentException();
            case GETTOPICSREQUEST:
                Assert.assertTrue(protoRequest.getGetTopicsRequest().getIdsList().equals(Arrays.asList(1, 2)));
                break;
            case PROTOREQUEST_NOT_SET:
                throw new IllegalArgumentException();
        }
    }

    //reuse object
    @Test
    public void shouldBeAbleToReuseObjectsAndRepeatedTypes() {
        Topic topic = getSampleTopic(1);
        VStatus.Builder statusBuilder = VStatus.newBuilder();
        VStatus vStatus = statusBuilder.setStatusCode(200).setMessage("OK").build();

        Topic topic1 = getSampleTopic(2);
//        VStatus.Builder statusBuilder1 = VStatus.newBuilder();
        VStatus vStatus1 = statusBuilder.setStatusCode(200).setMessage("OK").build();

        GetTopicResponse.Builder builder = GetTopicResponse.newBuilder();
        GetTopicResponse getTopicResponse = null;
        try {
            getTopicResponse = builder.setStatus(vStatus).setTopic(ProtoTopic.parseFrom(topic.toBytes())).build();
        } catch (InvalidProtocolBufferException e) {
        }

//        GetTopicResponse.Builder builder1 = GetTopicResponse.newBuilder();
        GetTopicResponse getTopicResponse1 = null;
        try {
            getTopicResponse1 = builder.setStatus(vStatus1).setTopic(ProtoTopic.parseFrom(topic.toBytes())).build();
        } catch (InvalidProtocolBufferException e) {
        }

        GetTopicsResponse.Builder finalBuilder = GetTopicsResponse.newBuilder();
        GetTopicsResponse getTopicsResponse = finalBuilder.addTopicResponses(getTopicResponse).addTopicResponses(getTopicResponse1).build();
        Assert.assertEquals(getTopicsResponse.getTopicResponsesCount(), 2);
        Assert.assertEquals(getTopicsResponse.getTopicResponses(0).getTopic().getId(), topic.id());
    }

    @Test
    public void buildObjectShouldBeFinal() {
        Topic topic = getSampleTopic(1);
        //? No setters in topic, only getters
    }

    private Topic getSampleTopic(int id) {
        ProtoTopic.Builder topicBuilder = ProtoTopic.newBuilder();
        topicBuilder.setId(id);
        topicBuilder.setName("topic-1");
        topicBuilder.setGrouped(true);
        topicBuilder.setPartitions(5);
        topicBuilder.setReplicationFactor(3);
        topicBuilder.setTopicCategory(TopicCategory.TOPIC);
        return new Topic(topicBuilder.build());
    }

}
