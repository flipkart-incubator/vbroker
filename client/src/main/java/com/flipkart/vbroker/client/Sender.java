package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.flatbuf.*;
import com.flipkart.vbroker.utils.FlatBuffers;
import com.google.common.primitives.Ints;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class Sender implements Runnable {

    private final Accumulator accumulator;
    private final Metadata metadata;
    private final NetworkClient client;
    private final CountDownLatch runningLatch;
    private volatile boolean running;

    public Sender(Accumulator accumulator,
                  Metadata metadata,
                  NetworkClient client) {
        this.accumulator = accumulator;
        this.metadata = metadata;
        this.client = client;

        running = true;
        runningLatch = new CountDownLatch(1);
    }

    @Override
    public void run() {
        while (running) {
            try {
                send();
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.error("Exception in send()", ex);
            }
        }
        runningLatch.countDown();
    }

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
    private void send() {
        List<Node> clusterNodes = metadata.getClusterNodes();
        log.info("ClusterNodes: {}", clusterNodes);

        clusterNodes.forEach(node -> {
            RecordBatch recordBatch = accumulator.getRecordBatch(node);
            int totalNoOfRecords = recordBatch.getTotalNoOfRecords();
            //if (totalNoOfRecords > 0) {
            log.info("Total no of records in batch for Node {} are {}", node.getBrokerId(), totalNoOfRecords);
            CompletionStage<Void> sendStage = sendRecordBatch(node, recordBatch);
            sendStage
                .thenAccept(aVoid -> log.info("RecordBatch to node {} is sent successfully", node))
                .exceptionally(throwable -> {
                    log.error("RecordBatch to node has failed during send", throwable);
                    return null;
                });
        });
    }

    /**
     * @param node        to send record to
     * @param recordBatch that has data to send
     */
    private CompletionStage<Void> sendRecordBatch(Node node, RecordBatch recordBatch) {
        VRequest vRequest = newVRequest(recordBatch);
        CompletionStage<VResponse> responseStage = client.send(node, vRequest);
        return responseStage
            .thenAccept(vResponse -> {
                recordBatch.setState(RecordBatch.BatchState.DONE_SUCCESS);
                log.info("Received vResponse with correlationId {}", vResponse.correlationId());

                parseProduceResponse(vResponse, recordBatch);
                log.info("Done parsing VResponse with correlationId {}", vResponse.correlationId());
            })
            .exceptionally(throwable -> {
                log.error("Exception in executing request {}: {}", vRequest.correlationId(), throwable.getMessage());
                recordBatch.setState(RecordBatch.BatchState.DONE_FAILURE);
                return null;
            });
    }

    private void parseProduceResponse(VResponse vResponse, RecordBatch recordBatch) {
        ProduceResponse produceResponse = (ProduceResponse) vResponse.responseMessage(new ProduceResponse());
        assert produceResponse != null;

        for (int i = 0; i < produceResponse.topicResponsesLength(); i++) {
            TopicProduceResponse topicProduceResponse = produceResponse.topicResponses(i);
            int topicId = topicProduceResponse.topicId();
            log.info("Handling ProduceResponse for topic {} with {} partition responses", topicId, topicProduceResponse.partitionResponsesLength());
            for (int j = 0; j < topicProduceResponse.partitionResponsesLength(); j++) {
                TopicPartitionProduceResponse partitionProduceResponse = topicProduceResponse.partitionResponses(j);
                //log.info("ProduceResponse for topic {} at partition {}", topicId, partitionProduceResponse);
                log.info("Response code for handling produceRequest for topic {} and partition {} is {}",
                    topicId, partitionProduceResponse.partitionId(), partitionProduceResponse.status().statusCode());


                TopicPartition topicPartition = metadata.getTopicPartition(topicProduceResponse.topicId(), partitionProduceResponse.partitionId());
                recordBatch.setResponse(topicPartition, partitionProduceResponse.status());
            }
        }
    }

    public void stop() {
        running = false;

        try {
            runningLatch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Killing Sender as time elapsed");
        }
    }

    private VRequest newVRequest(RecordBatch recordBatch) {
        FlatBufferBuilder builder = FlatBuffers.newBuilder();

        List<TopicPartReq> topicPartReqs =
            recordBatch.getTopicPartitionsWithRecords()
                .stream()
                .map(topicPartition -> {
                    List<Integer> msgOffsets =
                        recordBatch.getRecords(topicPartition)
                            .stream()
                            .filter(record -> recordBatch.isReady())
                            .map(record -> RecordUtils.flatBuffMsgOffset(builder, record, topicPartition.getId()))
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

        Map<Integer, List<TopicPartReq>> perTopicReqs = topicPartReqs.stream()
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
        int correlationId = Math.abs(new Random(10000).nextInt());
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
        private final int topicId;
        private final int topicPartProduceReqOffset;
    }
}
