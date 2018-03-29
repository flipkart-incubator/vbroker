package com.flipkart.vbroker.client;

import com.codahale.metrics.MetricRegistry;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.utils.MetricUtils;
import com.google.common.collect.PeekingIterator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
public class Accumulator implements PeekingIterator<Accumulator.RecordWithFuture> {

    private final BlockingQueue<RecordWithFuture> recordsQueue;

    public Accumulator(int capacity,
                       MetricRegistry metricRegistry) {
        this.recordsQueue = new ArrayBlockingQueue<>(capacity);
        metricRegistry.gauge(MetricUtils.clientFullMetricName("accumulator.records"), () -> recordsQueue::size);
    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode(exclude = {"future"})
    public class RecordWithFuture {
        private final ProducerRecord producerRecord;
        private final CompletableFuture<MessageMetadata> future;
    }

    /**
     * fetch latest metadata
     * get the RecordBatch to which a record can be added
     * add the record
     * <p>
     *
     * @param record to add
     * @return the CompletionStage with promised MessageMetadata
     */
    public CompletionStage<MessageMetadata> accumulateRecord(ProducerRecord record) {
        log.info("Adding record {} to accumulator", record.getMessageId());
        CompletableFuture<MessageMetadata> future = new CompletableFuture<>();
        RecordWithFuture recordWithFuture = new RecordWithFuture(record, future);
        if (!recordsQueue.offer(recordWithFuture)) {
            future.completeExceptionally(new VBrokerException("Unable to produce as producer client queue is full"));
        }
        return future;
    }

    @Override
    public RecordWithFuture peek() {
        return recordsQueue.peek();
    }

    @Override
    public boolean hasNext() {
        return !recordsQueue.isEmpty();
    }

    @Override
    public RecordWithFuture next() {
        return recordsQueue.poll();
    }

    @Override
    public void remove() {
        recordsQueue.remove();
    }
}
