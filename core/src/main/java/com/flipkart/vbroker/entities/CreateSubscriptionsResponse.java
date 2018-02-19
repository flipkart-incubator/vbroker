// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class CreateSubscriptionsResponse extends Table {
  public static CreateSubscriptionsResponse getRootAsCreateSubscriptionsResponse(ByteBuffer _bb) { return getRootAsCreateSubscriptionsResponse(_bb, new CreateSubscriptionsResponse()); }
  public static CreateSubscriptionsResponse getRootAsCreateSubscriptionsResponse(ByteBuffer _bb, CreateSubscriptionsResponse obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public CreateSubscriptionsResponse __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public CreateSubscriptionResponse createSubscriptionResponse(int j) { return createSubscriptionResponse(new CreateSubscriptionResponse(), j); }
  public CreateSubscriptionResponse createSubscriptionResponse(CreateSubscriptionResponse obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int createSubscriptionResponseLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }

  public static int createCreateSubscriptionsResponse(FlatBufferBuilder builder,
      int createSubscriptionResponseOffset) {
    builder.startObject(1);
    CreateSubscriptionsResponse.addCreateSubscriptionResponse(builder, createSubscriptionResponseOffset);
    return CreateSubscriptionsResponse.endCreateSubscriptionsResponse(builder);
  }

  public static void startCreateSubscriptionsResponse(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addCreateSubscriptionResponse(FlatBufferBuilder builder, int createSubscriptionResponseOffset) { builder.addOffset(0, createSubscriptionResponseOffset, 0); }
  public static int createCreateSubscriptionResponseVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startCreateSubscriptionResponseVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endCreateSubscriptionsResponse(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

