package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class Accumulator {

    private final long lingerTimeMs;
    private final VBClientConfig config;

    private Metadata metadata;
    private final Multimap<Node, RecordBatch> nodeRecordBatchMap = HashMultimap.create();

    public Accumulator(VBClientConfig config) {
        this.config = config;
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
    public RecordBatch getRecordBatch(Node node) {
        Optional<RecordBatch> batchOpt = nodeRecordBatchMap.get(node)
            .stream()
            .filter(recordBatch -> !recordBatch.isFull())
            .findFirst();

        RecordBatch batch;
        if (!batchOpt.isPresent()) {
            batch = RecordBatch.newInstance(node, lingerTimeMs);
            nodeRecordBatchMap.put(node, batch);
        } else {
            batch = batchOpt.get();
        }

        return batch;
    }

    public void addRecord(ProducerRecord record) {
        Metadata metadata = fetchMetadata();
        TopicPartition topicPartition = metadata.getTopicPartition(record.getTopicId(), record.getPartitionId());
        Node leaderNode = metadata.getLeaderNode(topicPartition);

        RecordBatch recordBatch = getRecordBatch(leaderNode);
        recordBatch.addRecord(topicPartition, record);
    }

    public void drain() {

    }
}
