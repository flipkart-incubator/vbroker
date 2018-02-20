// automatically generated by the FlatBuffers compiler, do not modify

package com.flipkart.vbroker.entities;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Subscription extends Table {
    public static Subscription getRootAsSubscription(ByteBuffer _bb) {
        return getRootAsSubscription(_bb, new Subscription());
    }

    public static Subscription getRootAsSubscription(ByteBuffer _bb, Subscription obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public static int createSubscription(FlatBufferBuilder builder,
                                         short id,
                                         short topicId,
                                         int nameOffset,
                                         boolean grouped,
                                         short parallelism,
                                         short requestTimeout,
                                         byte subscriptionType,
                                         byte subscriptionMechanism,
                                         int httpUriOffset,
                                         int httpMethodOffset,
                                         boolean elastic,
                                         int filterOperatorOffset,
                                         int filterKeyValuesListOffset,
                                         int callbackConfigOffset) {
        builder.startObject(14);
        Subscription.addCallbackConfig(builder, callbackConfigOffset);
        Subscription.addFilterKeyValuesList(builder, filterKeyValuesListOffset);
        Subscription.addFilterOperator(builder, filterOperatorOffset);
        Subscription.addHttpMethod(builder, httpMethodOffset);
        Subscription.addHttpUri(builder, httpUriOffset);
        Subscription.addName(builder, nameOffset);
        Subscription.addRequestTimeout(builder, requestTimeout);
        Subscription.addParallelism(builder, parallelism);
        Subscription.addTopicId(builder, topicId);
        Subscription.addId(builder, id);
        Subscription.addElastic(builder, elastic);
        Subscription.addSubscriptionMechanism(builder, subscriptionMechanism);
        Subscription.addSubscriptionType(builder, subscriptionType);
        Subscription.addGrouped(builder, grouped);
        return Subscription.endSubscription(builder);
    }

    public static void startSubscription(FlatBufferBuilder builder) {
        builder.startObject(14);
    }

    public static void addId(FlatBufferBuilder builder, short id) {
        builder.addShort(0, id, 0);
    }

    public static void addTopicId(FlatBufferBuilder builder, short topicId) {
        builder.addShort(1, topicId, 0);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(2, nameOffset, 0);
    }

    public static void addGrouped(FlatBufferBuilder builder, boolean grouped) {
        builder.addBoolean(3, grouped, false);
    }

    public static void addParallelism(FlatBufferBuilder builder, short parallelism) {
        builder.addShort(4, parallelism, 0);
    }

    public static void addRequestTimeout(FlatBufferBuilder builder, short requestTimeout) {
        builder.addShort(5, requestTimeout, 0);
    }

    public static void addSubscriptionType(FlatBufferBuilder builder, byte subscriptionType) {
        builder.addByte(6, subscriptionType, 0);
    }

    public static void addSubscriptionMechanism(FlatBufferBuilder builder, byte subscriptionMechanism) {
        builder.addByte(7, subscriptionMechanism, 0);
    }

    public static void addHttpUri(FlatBufferBuilder builder, int httpUriOffset) {
        builder.addOffset(8, httpUriOffset, 0);
    }

    public static void addHttpMethod(FlatBufferBuilder builder, int httpMethodOffset) {
        builder.addOffset(9, httpMethodOffset, 0);
    }

    public static void addElastic(FlatBufferBuilder builder, boolean elastic) {
        builder.addBoolean(10, elastic, false);
    }

    public static void addFilterOperator(FlatBufferBuilder builder, int filterOperatorOffset) {
        builder.addOffset(11, filterOperatorOffset, 0);
    }

    public static void addFilterKeyValuesList(FlatBufferBuilder builder, int filterKeyValuesListOffset) {
        builder.addOffset(12, filterKeyValuesListOffset, 0);
    }

    public static int createFilterKeyValuesListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]);
        return builder.endVector();
    }

    public static void startFilterKeyValuesListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addCallbackConfig(FlatBufferBuilder builder, int callbackConfigOffset) {
        builder.addOffset(13, callbackConfigOffset, 0);
    }

    public static int endSubscription(FlatBufferBuilder builder) {
        int o = builder.endObject();
        return o;
    }

    public void __init(int _i, ByteBuffer _bb) {
        bb_pos = _i;
        bb = _bb;
    }

    public Subscription __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public short id() {
        int o = __offset(4);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public short topicId() {
        int o = __offset(6);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public String name() {
        int o = __offset(8);
        return o != 0 ? __string(o + bb_pos) : null;
    }

    public ByteBuffer nameAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public boolean grouped() {
        int o = __offset(10);
        return o != 0 ? 0 != bb.get(o + bb_pos) : false;
    }

    public short parallelism() {
        int o = __offset(12);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public short requestTimeout() {
        int o = __offset(14);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public byte subscriptionType() {
        int o = __offset(16);
        return o != 0 ? bb.get(o + bb_pos) : 0;
    }

    public byte subscriptionMechanism() {
        int o = __offset(18);
        return o != 0 ? bb.get(o + bb_pos) : 0;
    }

    public String httpUri() {
        int o = __offset(20);
        return o != 0 ? __string(o + bb_pos) : null;
    }

    public ByteBuffer httpUriAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String httpMethod() {
        int o = __offset(22);
        return o != 0 ? __string(o + bb_pos) : null;
    }

    public ByteBuffer httpMethodAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public boolean elastic() {
        int o = __offset(24);
        return o != 0 ? 0 != bb.get(o + bb_pos) : false;
    }

    public String filterOperator() {
        int o = __offset(26);
        return o != 0 ? __string(o + bb_pos) : null;
    }

    public ByteBuffer filterOperatorAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public FilterKeyValues filterKeyValuesList(int j) {
        return filterKeyValuesList(new FilterKeyValues(), j);
    }

    public FilterKeyValues filterKeyValuesList(FilterKeyValues obj, int j) {
        int o = __offset(28);
        return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
    }

    public int filterKeyValuesListLength() {
        int o = __offset(28);
        return o != 0 ? __vector_len(o) : 0;
    }

    public CallbackConfig callbackConfig() {
        return callbackConfig(new CallbackConfig());
    }

    public CallbackConfig callbackConfig(CallbackConfig obj) {
        int o = __offset(30);
        return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null;
    }
}

