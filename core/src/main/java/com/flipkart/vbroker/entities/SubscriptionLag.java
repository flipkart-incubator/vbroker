// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class SubscriptionLag extends Table {
  public static SubscriptionLag getRootAsSubscriptionLag(ByteBuffer _bb) { return getRootAsSubscriptionLag(_bb, new SubscriptionLag()); }
  public static SubscriptionLag getRootAsSubscriptionLag(ByteBuffer _bb, SubscriptionLag obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public SubscriptionLag __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public short subscriptionId() { int o = __offset(4); return o != 0 ? bb.getShort(o + bb_pos) : 0; }
  public short topicId() { int o = __offset(6); return o != 0 ? bb.getShort(o + bb_pos) : 0; }
  public PartitionLag partitionLags(int j) { return partitionLags(new PartitionLag(), j); }
  public PartitionLag partitionLags(PartitionLag obj, int j) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int partitionLagsLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }

  public static int createSubscriptionLag(FlatBufferBuilder builder,
      short subscriptionId,
      short topicId,
      int partitionLagsOffset) {
    builder.startObject(3);
    SubscriptionLag.addPartitionLags(builder, partitionLagsOffset);
    SubscriptionLag.addTopicId(builder, topicId);
    SubscriptionLag.addSubscriptionId(builder, subscriptionId);
    return SubscriptionLag.endSubscriptionLag(builder);
  }

  public static void startSubscriptionLag(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addSubscriptionId(FlatBufferBuilder builder, short subscriptionId) { builder.addShort(0, subscriptionId, 0); }
  public static void addTopicId(FlatBufferBuilder builder, short topicId) { builder.addShort(1, topicId, 0); }
  public static void addPartitionLags(FlatBufferBuilder builder, int partitionLagsOffset) { builder.addOffset(2, partitionLagsOffset, 0); }
  public static int createPartitionLagsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startPartitionLagsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endSubscriptionLag(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

