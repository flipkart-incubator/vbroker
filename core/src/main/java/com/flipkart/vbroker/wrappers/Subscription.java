package com.flipkart.vbroker.wrappers;

import com.flipkart.vbroker.proto.ProtoSubscription;
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
public class Subscription {
    private final ProtoSubscription protoSubscription;

    public static Subscription fromJson(String protoSubscriptionJson){
        ProtoSubscription.Builder builder = ProtoSubscription.newBuilder();
        try {
            JsonFormat.parser().merge(protoSubscriptionJson, builder);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException();
        }
        return new Subscription(builder.build());
    }

    public static Subscription fromBytes(byte[] bytes){
        try {
            return new Subscription(ProtoSubscription.parseFrom(bytes));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ProtoSubscription)) {
            return super.equals(obj);
        }
        ProtoSubscription other = (ProtoSubscription) obj;

        return protoSubscription.getId() == other.getId();
    }

    @Override
    public int hashCode(){
        return protoSubscription.getId();
    }

    public String toJson(){
        try {
            return JsonFormat.printer().print(protoSubscription);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] toBytes(){
        return protoSubscription.toByteArray();
    }
}
