// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.flatbuf;

import java.nio.*;
import java.lang.*;

import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class TopicPartitionProduceResponse extends Table {
  public static TopicPartitionProduceResponse getRootAsTopicPartitionProduceResponse(ByteBuffer _bb) { return getRootAsTopicPartitionProduceResponse(_bb, new TopicPartitionProduceResponse()); }
  public static TopicPartitionProduceResponse getRootAsTopicPartitionProduceResponse(ByteBuffer _bb, TopicPartitionProduceResponse obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public TopicPartitionProduceResponse __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int partitionId() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public VStatus status() { return status(new VStatus()); }
  public VStatus status(VStatus obj) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }

  public static int createTopicPartitionProduceResponse(FlatBufferBuilder builder,
      int partitionId,
      int statusOffset) {
    builder.startObject(2);
    TopicPartitionProduceResponse.addStatus(builder, statusOffset);
    TopicPartitionProduceResponse.addPartitionId(builder, partitionId);
    return TopicPartitionProduceResponse.endTopicPartitionProduceResponse(builder);
  }

  public static void startTopicPartitionProduceResponse(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addPartitionId(FlatBufferBuilder builder, int partitionId) { builder.addInt(0, partitionId, 0); }
  public static void addStatus(FlatBufferBuilder builder, int statusOffset) { builder.addOffset(1, statusOffset, 0); }
  public static int endTopicPartitionProduceResponse(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

