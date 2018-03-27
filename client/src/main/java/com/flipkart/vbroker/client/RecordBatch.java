package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.flatbuf.VStatus;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A set of records going to a particular node in the cluster
 * records can belong to multiple topic-partitions within the broker
 * each topic partition can have multiple messages
 */
@Slf4j
@ToString
public class RecordBatch {
    private final Node node;
    private final long lingerTimeMs;
    private final long createdTimeMs;

    //private final Multimap<TopicPartition, ProducerRecord> partRecordsMap = ArrayListMultimap.create();
    //private final ConcurrentMap<TopicPartition, CompletableFuture<TopicPartMetadata>> topicPartResponseFutureMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<TopicPartition, FutureWithRecords> topicPartFuturesMap = new ConcurrentHashMap<>();

    //map which stores the responses for the batch sent
    private final Map<TopicPartition, VStatus> statusMap = new HashMap<>();
    @Setter
    @Getter
    private BatchState state;

    private RecordBatch(Node node, long lingerTimeMs) {
        this.node = node;
        this.lingerTimeMs = lingerTimeMs;
        this.createdTimeMs = System.currentTimeMillis();

        this.state = BatchState.QUEUED;
    }

    public static RecordBatch newInstance(Node node, long lingerTimeMs) {
        log.info("Creating new RecordBatch for node {}", node);
        return new RecordBatch(node, lingerTimeMs);
    }


    @Getter
    private class FutureWithRecords {
        private final CompletableFuture<TopicPartMetadata> future = new CompletableFuture<>();
        private final List<ProducerRecord> producerRecords = new ArrayList<>();

        void addRecord(ProducerRecord producerRecord) {
            producerRecords.add(producerRecord);
        }
    }

    public CompletableFuture<TopicPartMetadata> addRecord(TopicPartition topicPartition, ProducerRecord record) {
        log.debug("Adding record {} into topic partition {}", record.getMessageId(), topicPartition);
        topicPartFuturesMap
            .computeIfAbsent(topicPartition, topicPartition1 -> new FutureWithRecords())
            .addRecord(record);
        return topicPartFuturesMap.get(topicPartition).getFuture();

        //partRecordsMap.put(topicPartition, record);
        //topicPartResponseFutureMap.putIfAbsent(topicPartition, new CompletableFuture<>());
        //return topicPartResponseFutureMap.get(topicPartition);
    }

    /**
     * @return true if batch size configured is reached
     */
    private boolean isFull() {
        //TODO: validate this and fix this
        return getTotalNoOfRecords() >= Integer.MAX_VALUE;
        //return partRecordsMap.values().size() >= Integer.MAX_VALUE;
    }

    /**
     * this determines if this batch can be sent if
     * 1. is batch is NOT IN one of the terminated states
     * 2. batch IS full (or)
     * 3. batch expired
     *
     * @return true if above satisfied
     */
    public boolean isReady() {
        return !isDone() && !isInFlight() && (isFull() || hasExpired());
    }

    /**
     * @return true is batch is IN one of the terminated states
     */
    private boolean isDone() {
        return BatchState.DONE_FAILURE.equals(state) || BatchState.DONE_SUCCESS.equals(state);
    }

    private boolean isInFlight() {
        return BatchState.IN_FLIGHT.equals(state);
    }

    /**
     * @return true if accumulation time of the batch is breached
     */
    private boolean hasExpired() {
        return (System.currentTimeMillis() - createdTimeMs) > lingerTimeMs;
    }

    public long getTotalNoOfRecords() {
        return topicPartFuturesMap.values()
            .stream()
            .mapToInt(futureWithRecords -> futureWithRecords.getProducerRecords().size())
            .sum();
        //return partRecordsMap.values().size();
    }

    public List<ProducerRecord> getRecords(TopicPartition topicPartition) {
        //return Lists.newArrayList(partRecordsMap.get(topicPartition));
        return Lists.newArrayList(topicPartFuturesMap.get(topicPartition).getProducerRecords());
    }

    public Set<TopicPartition> getTopicPartitionsWithRecords() {
        return topicPartFuturesMap.keySet();
        //return partRecordsMap.keySet();
    }

    public void setResponse(TopicPartition topicPartition, VStatus status) {
//        if (status.statusCode() >= 0) {
//            throw new VBrokerException("dummy exception");
//        }
        statusMap.put(topicPartition, status);
        TopicPartMetadata topicPartMetadata =
            new TopicPartMetadata(topicPartition.getId(), status.statusCode());

        //topicPartResponseFutureMap.get(topicPartition).complete(topicPartMetadata);

        CompletableFuture<TopicPartMetadata> future = topicPartFuturesMap.get(topicPartition).getFuture();
        log.info("Completing TopicPartFuture {} for topicPartition {} with metadata {}",
            future, topicPartition, topicPartMetadata);
        future.complete(topicPartMetadata);
    }

    public VStatus getStatus(TopicPartition topicPartition) {
        return statusMap.get(topicPartition);
    }

    public enum BatchState {
        QUEUED, IN_FLIGHT, SENT, DONE_SUCCESS, DONE_FAILURE
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public class TopicPartMetadata {
        int partitionId;
        int statusCode;
    }
}
