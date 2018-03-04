package com.flipkart.vbroker.wrappers;

import com.flipkart.vbroker.proto.ProtoQueue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

/**
 * Created by kaushal.hooda on 02/03/18.
 */
public class Queue {
    private final ProtoQueue protoQueue;
    private final Topic topic;
    private final Subscription subscription;

    public Queue(ProtoQueue protoQueue) {
        this.protoQueue = protoQueue;
        this.topic = new Topic(protoQueue.getTopic());
        this.subscription = new Subscription(protoQueue.getSubscription());
    }

    public static Queue fromJson(String protoQueueJson) {
        ProtoQueue.Builder builder = ProtoQueue.newBuilder();
        try {
            JsonFormat.parser().merge(protoQueueJson, builder);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException();
        }
        return new Queue(builder.build());
    }

    public static Queue fromBytes(byte[] bytes) {
        try {
            return new Queue(ProtoQueue.parseFrom(bytes));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public int id() {
        return protoQueue.getId();
    }

    public Topic topic() {
        return new Topic(protoQueue.getTopic());
    }

    public Subscription subscription() {
        return new Subscription(protoQueue.getSubscription());
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
    public int hashCode() {
        return protoQueue.getId();
    }

    public String toJson() {
        try {
            return JsonFormat.printer().print(protoQueue);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] toBytes() {
        return protoQueue.toByteArray();
    }

}
