package com.flipkart.vbroker.client;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.flatbuf.*;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.utils.MetricUtils;
import com.flipkart.vbroker.utils.RandomUtils;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
public class Sender implements Runnable {

    private final Accumulator accumulator;
    private Metadata metadata;
    private final NetworkClient client;
    private final CountDownLatch runningLatch;
    private volatile boolean running;
    private final Partitioner partitioner;
    private final VBClientConfig config;
    private final Timer batchSendTimer;
    private final Counter sendErrorCounter;
    private final Histogram batchMessageCountHistogram;

    private final Object fetchMetadataMonitor = new Object();
    private volatile boolean isMetadataFetchInProgress = false;

    private final ConcurrentHashMap<Node, RecordBatch> nodeRecordBatchMap = new ConcurrentHashMap<>();
    private final Map<Node, Queue<RecordBatch>> nodeReadyRecordBatchMap = new HashMap<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public Sender(Accumulator accumulator,
                  NetworkClient client,
                  Partitioner partitioner,
                  VBClientConfig config,
                  MetricRegistry metricRegistry) {
        this.accumulator = accumulator;
        this.client = client;
        this.partitioner = partitioner;
        this.config = config;
        this.batchSendTimer = metricRegistry.timer(MetricUtils.clientFullMetricName("batch.send"));
        this.sendErrorCounter = metricRegistry.counter(MetricUtils.clientFullMetricName("batch.send.errors"));
        this.batchMessageCountHistogram = metricRegistry.histogram(MetricUtils.clientFullMetricName("batch.messages.count"));
        running = true;
        runningLatch = new CountDownLatch(1);

        forceRefreshMetadata();
    }

    @Override
    public void run() {
        while (running) {
            try {
                metadata = fetchMetadata();
                metadata.getClusterNodes()
                    .forEach(this::markReadyOldAndCreateNewBatch);
                prepareRecordBatches();
                send();
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.error("Exception in send()", ex);
            }
        }
        runningLatch.countDown();
    }

    private Metadata fetchMetadata() {
        if (metadata.aliveTimeMs() > config.getMetadataExpiryTimeMs()) {
            log.info("Metadata alive since {}ms and is greater than expiry time {}ms",
                metadata.aliveTimeMs(), config.getMetadataExpiryTimeMs());
            forceRefreshMetadata();
        }
        return metadata;
    }

    /**
     * this call blocks to fetch metadata
     */
    private void forceRefreshMetadata() {
        if (!isMetadataFetchInProgress) {
            synchronized (fetchMetadataMonitor) {
                //double check this multiple threads would have entered the first if condition
                if (!isMetadataFetchInProgress) {
                    isMetadataFetchInProgress = true;
                    log.info("Force populating Metadata");
                    metadata = new MetadataImpl(config);
                    isMetadataFetchInProgress = false;
                }
            }
        }
    }

    private void prepareRecordBatches() {
        //TODO: also add a throughput config parameter which can decide how many records in total to prepare before send
        log.info("Accumulator has {} records", accumulator.size());
        while (accumulator.hasNext()) {
            Accumulator.RecordWithFuture recordWithFuture = accumulator.peek();
            ProducerRecord record = recordWithFuture.getProducerRecord();

            TopicPartition partition = partitioner.partition(metadata.getTopic(record.getTopicId()), record);
            TopicPartition topicPartition = metadata.getTopicPartition(record.getTopicId(), partition.getId());
            Node leaderNode = metadata.getLeaderNode(topicPartition);

            RecordBatch recordBatch = markReadyOldAndCreateNewBatch(leaderNode);
            recordBatch.addRecord(topicPartition, record)
                .thenAcceptAsync(topicPartMetadata -> {
                    MessageMetadata messageMetadata = new MessageMetadata(record.getMessageId(),
                        record.getTopicId(),
                        topicPartMetadata.getPartitionId(),
                        -1);
                    recordWithFuture.getFuture().complete(messageMetadata);
                }, executorService)
                .exceptionally(throwable -> {
                    recordWithFuture.getFuture().completeExceptionally(throwable);
                    return null;
                });

            accumulator.next();
        }
    }

    private RecordBatch markReadyOldAndCreateNewBatch(Node leaderNode) {
        RecordBatch recordBatch = nodeRecordBatchMap
            .computeIfAbsent(leaderNode, leaderNode1 -> newRecordBatch(leaderNode));
        if (recordBatch.isReady()) {
            recordBatch.printStates();
            log.info("RecordBatch {} for node {} isReady with {} records",
                recordBatch.hashCode(), leaderNode, recordBatch.getTotalNoOfRecords());
            getRecordBatchQueue(leaderNode).offer(recordBatch);
            recordBatch = newRecordBatch(leaderNode);
            nodeRecordBatchMap.put(leaderNode, recordBatch);
        }

        return recordBatch;
    }

    private RecordBatch newRecordBatch(Node leaderNode) {
        return RecordBatch.newInstance(leaderNode,
            config.getLingerTimeMs(),
            config.getMaxBatchRecords(),
            config.getMaxBatchSizeBytes());
    }

    private Queue<RecordBatch> getRecordBatchQueue(Node node) {
        return nodeReadyRecordBatchMap
            .computeIfAbsent(node, node1 -> new ArrayDeque<>());
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
        log.debug("ClusterNodes: {}", clusterNodes);

        clusterNodes.forEach(node -> {
            Queue<RecordBatch> recordBatchQueue = getRecordBatchQueue(node);
            while (!recordBatchQueue.isEmpty()) {
                RecordBatch recordBatch = recordBatchQueue.poll();
                if (nonNull(recordBatch)) {
                    log.info("{} records are ready to be send for node {}", recordBatch.getTotalNoOfRecords(), node);
                    batchMessageCountHistogram.update(recordBatch.getTotalNoOfRecords());
                    sendReadyBatch(node, recordBatch);
                }
            }
        });
    }

    private void sendReadyBatch(Node node, RecordBatch recordBatch) {
        long totalNoOfRecords = recordBatch.getTotalNoOfRecords();
        log.debug("Total no of records in batch for Node {} are {}", node.getBrokerId(), totalNoOfRecords);
        VRequest vRequest = newVRequest(recordBatch);
        Timer.Context context = batchSendTimer.time();

        recordBatch.setState(RecordBatch.BatchState.IN_FLIGHT);
        CompletionStage<VResponse> responseStage = client.send(node, vRequest);
        responseStage
            .thenAccept(vResponse -> {
                long timeTakenNs = context.stop();
                recordBatch.setState(RecordBatch.BatchState.DONE_SUCCESS);
                log.info("Received vResponse for batch request of {} records with correlationId {} in {}ms",
                    totalNoOfRecords, vResponse.correlationId(), timeTakenNs / Math.pow(10, 6));

                try {
                    parseProduceResponse(vResponse, recordBatch);
                } catch (Throwable throwable) {
                    log.error("Exception in parsing vResponse with correlationId {}", vResponse.correlationId(), throwable);
                    throw throwable;
                }
                log.info("Done parsing VResponse with correlationId {}", vResponse.correlationId());
            })
            .exceptionally(throwable -> {
                log.error("Exception in executing request {}: {}", vRequest.correlationId(), throwable.getMessage());
                sendErrorCounter.inc();
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
            log.info("Handling ProduceResponse for topic {} with {} topicResponses and {} partitionResponses",
                topicId, produceResponse.topicResponsesLength(), topicProduceResponse.partitionResponsesLength());
            for (int j = 0; j < topicProduceResponse.partitionResponsesLength(); j++) {
                TopicPartitionProduceResponse partitionProduceResponse = topicProduceResponse.partitionResponses(j);
                //log.info("ProduceResponse for topic {} at partition {}", topicId, partitionProduceResponse);
                log.debug("Response code for handling produceRequest with correlationId {} for topic {} and partition {} is {}",
                    vResponse.correlationId(), topicId, partitionProduceResponse.partitionId(), partitionProduceResponse.status().statusCode());

                TopicPartition topicPartition = metadata.getTopicPartition(topicProduceResponse.topicId(), partitionProduceResponse.partitionId());
                log.debug("Got topicPartition {}", topicPartition);

                recordBatch.setResponse(topicPartition, partitionProduceResponse.status());
                log.debug("Done setting RecordBatch response status to {}", partitionProduceResponse.status().statusCode());
            }
        }
    }

    public void stop() {
        running = false;

        try {
            runningLatch.await(5000, TimeUnit.MILLISECONDS);
            MoreExecutors.shutdownAndAwaitTermination(executorService, 2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Killing Sender as time elapsed");
            executorService.shutdownNow(); //even this is not fool-proof
        }
    }

    private VRequest newVRequest(RecordBatch recordBatch) {
        FlatBufferBuilder builder = FlatbufUtils.newBuilder();

        List<TopicPartReq> topicPartReqs =
            recordBatch.getTopicPartitionsWithRecords()
                .stream()
                .map(topicPartition -> getTopicPartReq(builder, recordBatch, topicPartition))
                .collect(Collectors.toList());

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
        int correlationId = RandomUtils.generateRandomCorrelationId();
        int vRequest = VRequest.createVRequest(builder,
            (byte) 1,
            correlationId,
            RequestMessage.ProduceRequest,
            produceRequest);
        builder.finish(vRequest);

        return VRequest.getRootAsVRequest(builder.dataBuffer());
    }

    private TopicPartReq getTopicPartReq(FlatBufferBuilder builder,
                                         RecordBatch recordBatch,
                                         TopicPartition topicPartition) {
        List<Integer> msgOffsets =
            recordBatch.getRecordStream(topicPartition)
                //.stream()
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
    }

    @AllArgsConstructor
    @Getter
    private class TopicPartReq {
        private final int topicId;
        private final int topicPartProduceReqOffset;
    }
}
