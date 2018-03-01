package com.flipkart.vbroker.protocol;

import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.utils.TopicUtils;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kaushal.hooda on 09/01/18.
 */
public class VRequestCodecTest {
    @BeforeMethod
    public void setUp() {

    }

    private ByteBuffer getSampleProduceRequestAsFlatbuf() {

        FlatBufferBuilder builder = new FlatBufferBuilder();

        List<Topic> topics = new LinkedList<>();
        topics.add(DummyEntities.groupedTopic);

        int[] topicRequests = new int[topics.size()];
        for (int tIdx = 0; tIdx < topics.size(); tIdx++) {
            Topic topic = topics.get(tIdx);
            List<TopicPartition> partitions = TopicUtils.getTopicPartitions(topic);
            int[] partitionRequests = new int[partitions.size()];

            for (int i = 0; i < partitions.size(); i++) {
                TopicPartition topicPartition = partitions.get(i);
                int noOfMessages = 1;

                int[] messages = new int[noOfMessages];
                for (int m = 0; m < noOfMessages; m++) {
                    messages[m] = MessageStore.getSampleMsg(builder);
                }

                int messagesVector = MessageSet.createMessagesVector(builder, messages);
                int messageSet = MessageSet.createMessageSet(builder, messagesVector);
                int topicPartitionProduceRequest = TopicPartitionProduceRequest.createTopicPartitionProduceRequest(
                    builder,
                    topicPartition.getId(),
                    (short) 1,
                    messageSet);
                partitionRequests[i] = topicPartitionProduceRequest;
            }

            int partitionRequestsVector = TopicProduceRequest.createPartitionRequestsVector(builder, partitionRequests);
            int topicProduceRequest = TopicProduceRequest.createTopicProduceRequest(builder, topic.id(), partitionRequestsVector);
            topicRequests[tIdx] = topicProduceRequest;
        }

        int topicRequestsVector = ProduceRequest.createTopicRequestsVector(builder, topicRequests);
        int produceRequest = ProduceRequest.createProduceRequest(builder, topicRequestsVector);
        int vRequest = VRequest.createVRequest(builder,
            (byte) 1,
            1001,
            RequestMessage.ProduceRequest,
            produceRequest);
        builder.finish(vRequest);
        return builder.dataBuffer();
    }

    @Test
    public void shouldEncodeDecodeProduceRequest() throws InterruptedException {
        EmbeddedChannel decoder = new EmbeddedChannel(new VRequestDecoder());
        EmbeddedChannel encoder = new EmbeddedChannel(new VRequestEncoder());

        ByteBuffer byteBuffer = getSampleProduceRequestAsFlatbuf();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
        Request request = new Request(byteBuf.readableBytes(), byteBuf);

        //Encode the request
        encoder.writeAndFlush(request);
        Object out = encoder.readOutbound();

        //Decode the request
        decoder.writeInbound(out);
        Object decoded = decoder.readInbound();

        //Verify
        ProduceRequest produceRequest = (ProduceRequest) ((VRequest) decoded).requestMessage(new ProduceRequest());
        assert produceRequest != null;
        Message message = produceRequest.topicRequests(0).partitionRequests(0).messageSet().messages(0);
        Assert.assertEquals(message.topicId(), 101);
    }
}
