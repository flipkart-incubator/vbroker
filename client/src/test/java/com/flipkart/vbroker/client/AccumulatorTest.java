package com.flipkart.vbroker.client;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;

@Slf4j
public class AccumulatorTest {

    private Accumulator accumulator;

    @BeforeMethod
    public void setUp() {
        accumulator = new Accumulator(10, mock(MetricRegistry.class));
    }

    @Test
    public void shouldAddRecordsToAccumulator_IterateOverThem() {
        int noOfRecords = 3;
        List<ProducerRecord> records = Lists.newArrayList();

        IntStream.range(0, noOfRecords)
            .forEach(i -> {
                ProducerRecord record = RecordUtils.newProducerRecord(MessageStore.getRandomMsg("group_1"));
                records.add(record);
                accumulator.accumulateRecord(record)
                    .toCompletableFuture()
                    .complete(new MessageMetadata(record.getMessageId(), record.getTopicId(), -1, -1));
            });

        assertEquals(accumulator.size(), noOfRecords);

        int i = 0;
        while (accumulator.hasNext()) {
            Accumulator.RecordWithFuture recordWithFuture = accumulator.next();
            ProducerRecord record = recordWithFuture.getProducerRecord();
            log.info("Consumed record with msg_id {} and group_id {} from accumulator",
                record.getMessageId(), record.getGroupId());
            assertEquals(record, records.get(i));
            i++;
        }

        assertEquals(i, noOfRecords);
    }
}