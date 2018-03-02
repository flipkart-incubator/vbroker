package com.flipkart.vbroker.wrappers;

import com.flipkart.vbroker.proto.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Created by kaushal.hooda on 02/03/18.
 */
@AllArgsConstructor
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

    public int id(){
        return protoSubscription.getId();
    }

    public int topicId(){
        return protoSubscription.getTopicId();
    }

    public String name(){
        return protoSubscription.getName();
    }

    public boolean grouped(){
        return protoSubscription.getGrouped();
    }

    public int parallelism(){
        return protoSubscription.getParallelism();
    }

    public int requestTimeout(){
        return protoSubscription.getRequestTimeout();
    }

    public SubscriptionType subscriptionType(){
        return protoSubscription.getSubscriptionType();
    }

    public SubscriptionMechanism subscriptionMechanism(){
        return protoSubscription.getSubscriptionMechanism();
    }

    public String httpUri(){
        return protoSubscription.getHttpUri();
    }

    public HttpMethod httpMethod(){
        return protoSubscription.getHttpMethod();
    }

    public boolean elastic(){
        return protoSubscription.getElastic();
    }

    public FilterOperator filterOperator(){
        return protoSubscription.getFilterOperator();
    }

    public List<FilterKeyValues> filterKeyValuesList(){
        return protoSubscription.getFilterKeyValuesListList();
    }

    public CallbackConfig callbackConfig(){
        return protoSubscription.getCallbackConfig();
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
