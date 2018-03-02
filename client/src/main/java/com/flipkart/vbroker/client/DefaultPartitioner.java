package com.flipkart.vbroker.client;

import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.utils.TopicUtils;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class DefaultPartitioner implements Partitioner {
    private static final HashFunction hashFunction = Hashing.goodFastHash(32);

    @Override
    public TopicPartition partition(Topic topic, ProducerRecord record) {
        short partitionId = (short) Math.abs(hashFunction.
            hashUnencodedChars(record.getGroupId()).asInt() % topic.partitions());
        return TopicUtils.getTopicPartition(topic, partitionId);
    }
}
