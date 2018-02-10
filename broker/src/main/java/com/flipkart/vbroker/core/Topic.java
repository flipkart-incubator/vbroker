package com.flipkart.vbroker.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.flipkart.vbroker.utils.JsonUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hooda on 19/1/18
 */
@Getter
@JsonDeserialize(builder = Topic.TopicBuilder.class)
public class Topic {
    public static final short DEFAULT_NO_OF_PARTITIONS = 3;
    public static final short DEFAULT_REPLICATION_FACTOR = 3;
    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();
    private final short id;
    private final int noOfPartitions;
    private final int replicationFactor;
    private final boolean grouped;
    private final String name;
    private final TopicCategory topicCategory;
    private final List<TopicPartition> partitions;

    public Topic(short id, String name) {
        this.id = id;
        this.name = name;
        this.noOfPartitions = DEFAULT_NO_OF_PARTITIONS;
        this.replicationFactor = DEFAULT_REPLICATION_FACTOR;
        this.grouped = false;
        this.topicCategory = TopicCategory.TOPIC;
        this.partitions = new ArrayList<>();

    }

    public Topic(short id, int noOfPartitions, int replicationFactor, boolean grouped, String name, TopicCategory topicCategory, List<TopicPartition> partitions) {
        this.id = id;
        this.noOfPartitions = noOfPartitions;
        this.replicationFactor = replicationFactor;
        this.grouped = grouped;
        this.name = name;
        this.topicCategory = topicCategory;
        this.partitions = partitions;
    }

    //make it package access
    public void addPartition(TopicPartition topicPartition) {
        this.partitions.add(topicPartition);
    }

    public TopicPartition getPartition(short id) {
        return partitions.get(id);
    }

    public String toJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }

    public byte[] toBytes() throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic)) return false;
        final Topic topic = (Topic) o;
        return id == topic.id && name == topic.name;
    }

    public enum TopicCategory {
        QUEUE, TOPIC
    }

    @JsonPOJOBuilder
    public static final class TopicBuilder {
        private short id;
        private int noOfPartitions;
        private int replicationFactor;
        private boolean grouped;
        private String name;
        private TopicCategory topicCategory;
        private List<TopicPartition> partitions;

        private TopicBuilder() {
        }

        public static TopicBuilder aTopic() {
            return new TopicBuilder();
        }

        public TopicBuilder withId(short id) {
            this.id = id;
            return this;
        }

        public TopicBuilder withNoOfPartitions(int noOfPartitions) {
            this.noOfPartitions = noOfPartitions;
            return this;
        }

        public TopicBuilder withReplicationFactor(int replicationFactor) {
            this.replicationFactor = replicationFactor;
            return this;
        }

        public TopicBuilder withGrouped(boolean grouped) {
            this.grouped = grouped;
            return this;
        }

        public TopicBuilder withName(String name) {
            this.name = name;
            return this;
        }


        public TopicBuilder withTopicCategory(TopicCategory topicCategory) {
            this.topicCategory = topicCategory;
            return this;
        }

        public TopicBuilder withPartitions(List<TopicPartition> partitions) {
            this.partitions = partitions;
            return this;
        }

        public Topic build() {
            return new Topic(id, noOfPartitions, replicationFactor, grouped, name, topicCategory, partitions);
        }
    }
}
