package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Optional;

public class Accumulator {

    private final long lingerTimeMs;
    private Multimap<Node, RecordBatch> nodeRecordBatchMap = HashMultimap.create();

    public Accumulator(long lingerTimeMs) {
        this.lingerTimeMs = lingerTimeMs;
    }

    public Metadata fetchMetadata() {
        return null;
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
