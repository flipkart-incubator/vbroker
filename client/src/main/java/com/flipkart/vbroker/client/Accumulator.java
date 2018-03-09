package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
public class Accumulator {

    private final long lingerTimeMs;
    private final VBClientConfig config;
    private final Partitioner partitioner;
    private final Multimap<Node, RecordBatch> nodeRecordBatchMap = HashMultimap.create();
    private final Map<RecordBatch, CompletionStage<VResponse>> batchResponseStageMap = new HashMap<>();
    private Metadata metadata;

    public Accumulator(VBClientConfig config,
                       Partitioner partitioner) {
        this.config = config;
        this.partitioner = partitioner;
        this.lingerTimeMs = 4000;

        forceRefreshMetadata();
    }

    public Metadata fetchMetadata() {
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
        log.info("Force populating Metadata");
        metadata = new MetadataImpl(config);
    }

    /**
     * returns the available (current/next) batch for the leader node
     *
     * @param node to get batch for
     * @return the batch
     */
    private synchronized RecordBatch getRecordBatch(Node node) {
        Optional<RecordBatch> batchOpt = nodeRecordBatchMap.get(node)
            .stream()
            .filter(recordBatch -> !recordBatch.isReady())
            .findFirst();

        RecordBatch batch;
        if (!batchOpt.isPresent()) {
            batch = RecordBatch.newInstance(node, lingerTimeMs);
            nodeRecordBatchMap.put(node, batch);
            batchResponseStageMap.put(batch, new CompletableFuture<>());
        } else {
            batch = batchOpt.get();
        }

        return batch;
    }

    /**
     * returns the ready batch that is ready to be sent by the Sender
     *
     * @param node to get batch for
     * @return empty if not found and batch if found
     */
    public synchronized Optional<RecordBatch> getReadyRecordBatch(Node node) {
        return nodeRecordBatchMap.get(node)
            .stream()
            .filter(RecordBatch::isReady)
            .findFirst();
    }

    /**
     * fetch latest metadata
     * get the RecordBatch to which a record can be added
     * add the record
     *
     * @param record to add
     * @return the CompletionStage with promised MessageMetadata
     */
    public synchronized CompletionStage<MessageMetadata> accumulateRecord(ProducerRecord record) {
        log.info("Adding record {} to accumulator", record.getMessageId());
        Metadata metadata = fetchMetadata();
        TopicPartition partition = partitioner.partition(metadata.getTopic(record.getTopicId()), record);
        TopicPartition topicPartition = metadata.getTopicPartition(record.getTopicId(), partition.getId());
        Node leaderNode = metadata.getLeaderNode(topicPartition);

        RecordBatch recordBatch = getRecordBatch(leaderNode);
        CompletableFuture<RecordBatch.TopicPartMetadata> future = recordBatch.addRecord(topicPartition, record);

        return future.thenApply(topicPartMetadata ->
                new MessageMetadata(
                    record.getMessageId(),
                    record.getTopicId(),
                    topicPartMetadata.getPartitionId(),
                    Math.abs(new Random().nextInt()))
        );
    }

    public synchronized void drain(RecordBatch recordBatch) {
        if (nodeRecordBatchMap.containsKey(recordBatch)) {
            log.info("Draining RecordBatch");
            nodeRecordBatchMap.removeAll(recordBatch);
        }
    }
}
