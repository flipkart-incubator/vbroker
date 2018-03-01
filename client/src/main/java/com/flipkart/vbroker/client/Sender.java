package com.flipkart.vbroker.client;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.protocol.Request;
import com.flipkart.vbroker.utils.FlatBuffers;
import com.google.common.primitives.Ints;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Sender implements Runnable {

    private final Accumulator accumulator;
    private final Metadata metadata;
    private final Bootstrap bootstrap;
    private final VBClientConfig config;

    public Sender(Accumulator accumulator,
                  Metadata metadata,
                  Bootstrap bootstrap,
                  VBClientConfig config) {
        this.accumulator = accumulator;
        this.metadata = metadata;
        this.bootstrap = bootstrap;
        this.config = config;
    }

    public ChannelFuture getChannelFuture(Node node) {
        return bootstrap.connect(node.getHostIp(), node.getHostPort());
    }

    @Override
    public void run() {
        /*
         * Logic:
         * 0. get all cluster nodes from metadata
         * 1. get all the topic partitions hosted by the node
         * 2. find the RecordBatch-es which are queued up in accumulator
         * 3. sum up all the records into a MessageSet within a RecordBatch for a particular topic partition
         * 5. aggregate the requests at a topic level
         * 6. prepare the ProduceRequest
         * 7. map the request to the response and assign the corresponding status code
         */
        List<Node> clusterNodes = metadata.getClusterNodes();
        clusterNodes.stream()
            .map(node -> {
                RecordBatch recordBatch = accumulator.getRecordBatch(node);

                VRequest vRequest = getVRequest(recordBatch);
                int correlationId = vRequest.correlationId();

                return getChannelFuture(node).addListener((ChannelFutureListener) future -> {
                    Channel channel = future.channel();

                    ByteBuf byteBuf = Unpooled.wrappedBuffer(vRequest.getByteBuffer());
                    Request request = new Request(byteBuf.readableBytes(), byteBuf);

                    channel.writeAndFlush(request).addListener((ChannelFutureListener) future1 -> {

                    });
                });
            });
    }

    private VRequest getVRequest(RecordBatch recordBatch) {
        FlatBufferBuilder builder = FlatBuffers.newBuilder();

        List<TopicPartReq> topicPartReqs =
            recordBatch.getTopicPartitionsWithRecords()
                .stream()
                .map(topicPartition -> {
                    List<Integer> msgOffsets =
                        recordBatch.getRecords(topicPartition)
                            .stream()
                            .filter(record -> recordBatch.isReady())
                            .map(record -> RecordUtils.flatBuffMsgOffset(builder, record))
                            .collect(Collectors.toList());
                    int[] messages = Ints.toArray(msgOffsets);

                    int messagesVector = MessageSet.createMessagesVector(builder, messages);
                    int messageSet = MessageSet.createMessageSet(builder, messagesVector);
                    int topicPartitionProduceRequest = TopicPartitionProduceRequest.createTopicPartitionProduceRequest(
                        builder,
                        topicPartition.getId(),
                        (short) 1,
                        messageSet);

                    return new TopicPartReq(topicPartition.getTopicId(), topicPartitionProduceRequest);
                }).collect(Collectors.toList());

        Map<Short, List<TopicPartReq>> perTopicReqs = topicPartReqs.stream()
            .collect(Collectors.groupingBy(TopicPartReq::getTopicId));
        List<Integer> topicOffsetList = perTopicReqs.entrySet()
            .stream()
            .map(entry -> {
                List<Integer> topicPartProduceReqOffsets =
                    perTopicReqs.get(entry.getKey()).stream().map(TopicPartReq::getTopicPartProduceReqOffset).collect(Collectors.toList());
                int[] partReqOffsets = Ints.toArray(topicPartProduceReqOffsets);
                int partitionRequestsVector = TopicProduceRequest.createPartitionRequestsVector(builder, partReqOffsets);

                return TopicProduceRequest.createTopicProduceRequest(builder, entry.getKey(), partitionRequestsVector);
            }).collect(Collectors.toList());

        int[] topicRequests = Ints.toArray(topicOffsetList);
        int topicRequestsVector = ProduceRequest.createTopicRequestsVector(builder, topicRequests);
        int produceRequest = ProduceRequest.createProduceRequest(builder, topicRequestsVector);
        int correlationId = new Random(10000).nextInt();
        int vRequest = VRequest.createVRequest(builder,
            (byte) 1,
            correlationId,
            RequestMessage.ProduceRequest,
            produceRequest);
        builder.finish(vRequest);

        return VRequest.getRootAsVRequest(builder.dataBuffer());
    }

    @AllArgsConstructor
    @Getter
    private class TopicPartReq {
        private final short topicId;
        private final int topicPartProduceReqOffset;
    }
}
