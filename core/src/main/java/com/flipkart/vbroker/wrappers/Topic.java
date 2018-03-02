package com.flipkart.vbroker.wrappers;

import com.flipkart.vbroker.proto.ProtoTopic;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by kaushal.hooda on 02/03/18.
 */
@AllArgsConstructor
@Getter
public class Topic {
    private final ProtoTopic protoTopic;

    public static Topic fromJson(String protoTopicJson){
        ProtoTopic.Builder topicBuilder = ProtoTopic.newBuilder();
        try {
            JsonFormat.parser().merge(protoTopicJson, topicBuilder);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException();
        }
        return new Topic(topicBuilder.build());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ProtoTopic)) {
            return super.equals(obj);
        }
        ProtoTopic other = (ProtoTopic) obj;

        return protoTopic.getId() == other.getId();
    }

    @Override
    public int hashCode(){
        return protoTopic.getId();
    }

    public String toJson(){
        try {
            return JsonFormat.printer().print(protoTopic);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
