// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class TopicCreateRequest extends Table {
    public static TopicCreateRequest getRootAsTopicCreateRequest(ByteBuffer _bb) {
        return getRootAsTopicCreateRequest(_bb, new TopicCreateRequest());
    }

    public static TopicCreateRequest getRootAsTopicCreateRequest(ByteBuffer _bb, TopicCreateRequest obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public static int createTopicCreateRequest(FlatBufferBuilder builder,
                                               byte topicId) {
        builder.startObject(1);
        TopicCreateRequest.addTopicId(builder, topicId);
        return TopicCreateRequest.endTopicCreateRequest(builder);
    }

    public static void startTopicCreateRequest(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addTopicId(FlatBufferBuilder builder, byte topicId) {
        builder.addByte(0, topicId, 0);
    }

    public static int endTopicCreateRequest(FlatBufferBuilder builder) {
        int o = builder.endObject();
        return o;
    }

    public void __init(int _i, ByteBuffer _bb) {
        bb_pos = _i;
        bb = _bb;
    }

    public TopicCreateRequest __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public byte topicId() {
        int o = __offset(4);
        return o != 0 ? bb.get(o + bb_pos) : 0;
    }
}

