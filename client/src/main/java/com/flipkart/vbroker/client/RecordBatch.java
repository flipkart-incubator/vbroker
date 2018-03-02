package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.VStatus;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
    @Setter
    @Getter
    private BatchState state;

    @AllArgsConstructor
    @Getter
    public class TopicPartMetadata {
        short partitionId;
        short statusCode;
    }

    private final Multimap<TopicPartition, ProducerRecord> partRecordsMap = HashMultimap.create();
    private final Map<TopicPartition, CompletableFuture<TopicPartMetadata>> topicPartResponseFutureMap = new HashMap<>();

    public enum BatchState {
        QUEUED, SENT, DONE_SUCCESS, DONE_FAILURE
    }

    //map which stores the responses for the batch sent
    private final Map<TopicPartition, VStatus> statusMap = new HashMap<>();

    public RecordBatch(Node node, long lingerTimeMs) {
        this.node = node;
        this.lingerTimeMs = lingerTimeMs;
        this.createdTimeMs = System.currentTimeMillis();

        this.state = BatchState.QUEUED;
    }

    public static RecordBatch newInstance(Node node, long lingerTimeMs) {
        log.info("Creating new RecordBatch for node {}", node);
        return new RecordBatch(node, lingerTimeMs);
    }

    public CompletableFuture<TopicPartMetadata> addRecord(TopicPartition topicPartition, ProducerRecord record) {
        log.info("Adding record {} into topic partition {}", record.getMessageId(), topicPartition);
        partRecordsMap.put(topicPartition, record);
        topicPartResponseFutureMap.putIfAbsent(topicPartition, new CompletableFuture<>());
        return topicPartResponseFutureMap.get(topicPartition);
    }

    public boolean isFull() {
        return partRecordsMap.values().size() >= 1;
    }

    /**
     * this determines if this batch can be sent if
     * 1. batch is full (or)
     * 2. batch expired
     *
     * @return
     */
    public boolean isReady() {
        return isFull() || hasExpired();
    }

    private boolean hasExpired() {
        return (System.currentTimeMillis() - createdTimeMs) > lingerTimeMs;
    }

    public int getTotalNoOfRecords() {
        return partRecordsMap.values().size();
    }

    public List<ProducerRecord> getRecords(TopicPartition topicPartition) {
        return Lists.newArrayList(partRecordsMap.get(topicPartition));
    }

    public Set<TopicPartition> getTopicPartitionsWithRecords() {
        return partRecordsMap.keySet();
    }

    public void setResponse(TopicPartition topicPartition, VStatus status) {
        statusMap.put(topicPartition, status);
        TopicPartMetadata topicPartMetadata =
            new TopicPartMetadata(topicPartition.getId(), status.statusCode());
        topicPartResponseFutureMap.get(topicPartition).complete(topicPartMetadata);
    }

    public VStatus getStatus(TopicPartition topicPartition) {
        return statusMap.get(topicPartition);
    }
}
