package com.flipkart.vbroker.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@EqualsAndHashCode(exclude = {"partitions", "noOfPartitions", "replicationFactor", "grouped", "topicCategory"})
@ToString
public class Topic {
    public static final short DEFAULT_NO_OF_PARTITIONS = 3;
    public static final short DEFAULT_REPLICATION_FACTOR = 3;

    private final short id;
    private final List<TopicPartition> partitions = new ArrayList<>();
    @Setter
    private short noOfPartitions = DEFAULT_NO_OF_PARTITIONS;
    @Setter
    private short replicationFactor = DEFAULT_REPLICATION_FACTOR;
    @Setter
    private boolean grouped = false;
    @Setter
    private TopicCategory topicCategory = TopicCategory.TOPIC;

    public Topic(Short id) {
        this.id = id;
    }

    //make it package access
    public void addPartition(TopicPartition topicPartition) {
        this.partitions.add(topicPartition);
    }

    public TopicPartition getPartition(short id) {
        return partitions.get(id);
    }

    public enum TopicCategory {
        QUEUE, TOPIC
    }
}
