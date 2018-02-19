// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class PartitionLag extends Table {
    public static PartitionLag getRootAsPartitionLag(ByteBuffer _bb) {
        return getRootAsPartitionLag(_bb, new PartitionLag());
    }

    public static PartitionLag getRootAsPartitionLag(ByteBuffer _bb, PartitionLag obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public static int createPartitionLag(FlatBufferBuilder builder,
                                         short partitionId,
                                         int lag) {
        builder.startObject(2);
        PartitionLag.addLag(builder, lag);
        PartitionLag.addPartitionId(builder, partitionId);
        return PartitionLag.endPartitionLag(builder);
    }

    public static void startPartitionLag(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addPartitionId(FlatBufferBuilder builder, short partitionId) {
        builder.addShort(0, partitionId, 0);
    }

    public static void addLag(FlatBufferBuilder builder, int lag) {
        builder.addInt(1, lag, 0);
    }

    public static int endPartitionLag(FlatBufferBuilder builder) {
        int o = builder.endObject();
        return o;
    }

    public void __init(int _i, ByteBuffer _bb) {
        bb_pos = _i;
        bb = _bb;
    }

    public PartitionLag __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public short partitionId() {
        int o = __offset(4);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public int lag() {
        int o = __offset(6);
        return o != 0 ? bb.getInt(o + bb_pos) : 0;
    }
}

