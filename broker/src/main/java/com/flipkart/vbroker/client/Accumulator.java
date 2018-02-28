package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Optional;

public class Accumulator {

    private Multimap<Node, RecordBatch> nodeRecordBatchMap = HashMultimap.create();
    private Metadata metadata;

    public Accumulator() {
        //metadata = new
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
            batch = RecordBatch.newInstance(node);
            nodeRecordBatchMap.put(node, batch);
        } else {
            batch = batchOpt.get();
        }

        return batch;
    }

    public void addRecord(ProducerRecord record) {
        TopicPartition topicPartition = metadata.getTopicPartition(record.getTopicId(), record.getPartitionId());
        Node leaderNode = metadata.getLeaderNode(topicPartition);

        RecordBatch recordBatch = getRecordBatch(leaderNode);
        recordBatch.addRecord(topicPartition, record);
    }

    public void drain() {

    }
}
