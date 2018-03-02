package com.flipkart.vbroker.wrappers;

import com.flipkart.vbroker.proto.ProtoQueue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by kaushal.hooda on 02/03/18.
 */
@AllArgsConstructor
@Getter
public class Queue {
    private final ProtoQueue protoQueue;

    public static Queue fromJson(String protoQueueJson){
        ProtoQueue.Builder builder = ProtoQueue.newBuilder();
        try {
            JsonFormat.parser().merge(protoQueueJson, builder);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException();
        }
        return new Queue(builder.build());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ProtoQueue)) {
            return super.equals(obj);
        }
        ProtoQueue other = (ProtoQueue) obj;

        return protoQueue.getId() == other.getId();
    }

    @Override
    public int hashCode(){
        return protoQueue.getId();
    }

    public String toJson(){
        try {
            return JsonFormat.printer().print(protoQueue);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

}
