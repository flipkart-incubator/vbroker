package com.flipkart.vbroker.wrappers;

import com.flipkart.vbroker.proto.ProtoTopic;
import com.flipkart.vbroker.proto.TopicCategory;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.AllArgsConstructor;

/**
 * Created by kaushal.hooda on 02/03/18.
 */
@AllArgsConstructor
public class Topic {
    private final ProtoTopic protoTopic;

    public static Topic fromJson(String protoTopicJson) {
        ProtoTopic.Builder topicBuilder = ProtoTopic.newBuilder();
        try {
            JsonFormat.parser().merge(protoTopicJson, topicBuilder);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        return new Topic(topicBuilder.build());
    }

    public static Topic fromBytes(byte[] fromBytes) {
        try {
            return new Topic(ProtoTopic.parseFrom(fromBytes));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public int id() {
        return protoTopic.getId();
    }

    public String name() {
        return protoTopic.getName();
    }

    public boolean grouped() {
        return protoTopic.getGrouped();
    }

    public int partitions() {
        return protoTopic.getPartitions();
    }

    public int replicationFactor() {
        return protoTopic.getReplicationFactor();
    }

    public TopicCategory topicCategory() {
        return protoTopic.getTopicCategory();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Topic)) {
            return super.equals(obj);
        }
        Topic other = (Topic) obj;

        return protoTopic.getId() == other.id();
    }

    @Override
    public int hashCode() {
        return protoTopic.getId();
    }

    public String toJson() {
        try {
            return JsonFormat.printer().print(protoTopic);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] toBytes() {
        return protoTopic.toByteArray();
    }
}
