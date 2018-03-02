// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.flatbuf;

import java.nio.*;
import java.lang.*;

import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class TopicPartitionFetchResponse extends Table {
  public static TopicPartitionFetchResponse getRootAsTopicPartitionFetchResponse(ByteBuffer _bb) { return getRootAsTopicPartitionFetchResponse(_bb, new TopicPartitionFetchResponse()); }
  public static TopicPartitionFetchResponse getRootAsTopicPartitionFetchResponse(ByteBuffer _bb, TopicPartitionFetchResponse obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public TopicPartitionFetchResponse __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int partitionId() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public VStatus status() { return status(new VStatus()); }
  public VStatus status(VStatus obj) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  public MessageSet messageSet() { return messageSet(new MessageSet()); }
  public MessageSet messageSet(MessageSet obj) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }

  public static int createTopicPartitionFetchResponse(FlatBufferBuilder builder,
      int partitionId,
      int statusOffset,
      int messageSetOffset) {
    builder.startObject(3);
    TopicPartitionFetchResponse.addMessageSet(builder, messageSetOffset);
    TopicPartitionFetchResponse.addStatus(builder, statusOffset);
    TopicPartitionFetchResponse.addPartitionId(builder, partitionId);
    return TopicPartitionFetchResponse.endTopicPartitionFetchResponse(builder);
  }

  public static void startTopicPartitionFetchResponse(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addPartitionId(FlatBufferBuilder builder, int partitionId) { builder.addInt(0, partitionId, 0); }
  public static void addStatus(FlatBufferBuilder builder, int statusOffset) { builder.addOffset(1, statusOffset, 0); }
  public static void addMessageSet(FlatBufferBuilder builder, int messageSetOffset) { builder.addOffset(2, messageSetOffset, 0); }
  public static int endTopicPartitionFetchResponse(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

