// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class FetchResponse extends Table {
  public static FetchResponse getRootAsFetchResponse(ByteBuffer _bb) { return getRootAsFetchResponse(_bb, new FetchResponse()); }
  public static FetchResponse getRootAsFetchResponse(ByteBuffer _bb, FetchResponse obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public FetchResponse __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public TopicFetchResponse topicResponses(int j) { return topicResponses(new TopicFetchResponse(), j); }
  public TopicFetchResponse topicResponses(TopicFetchResponse obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int topicResponsesLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }

  public static int createFetchResponse(FlatBufferBuilder builder,
      int topicResponsesOffset) {
    builder.startObject(1);
    FetchResponse.addTopicResponses(builder, topicResponsesOffset);
    return FetchResponse.endFetchResponse(builder);
  }

  public static void startFetchResponse(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addTopicResponses(FlatBufferBuilder builder, int topicResponsesOffset) { builder.addOffset(0, topicResponsesOffset, 0); }
  public static int createTopicResponsesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startTopicResponsesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endFetchResponse(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

