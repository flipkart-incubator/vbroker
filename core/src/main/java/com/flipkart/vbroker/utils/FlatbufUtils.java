package com.flipkart.vbroker.utils;

/**
 * Created by kaushal.hooda on 23/02/18.
 */

import com.flipkart.vbroker.entities.PartitionLag;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.entities.SubscriptionLag;
import com.flipkart.vbroker.entities.VStatus;
import com.google.flatbuffers.FlatBufferBuilder;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Utility methods to generate flatbufs properly
 * given a builder and all of an entities' fields.
 * Returns the int offset after building.
 */
public class FlatbufUtils {

    public static int buildPartitionLag(FlatBufferBuilder builder, short partitionId, int lag, VStatus vStatus){
        int vStatusOffset = buildVStatus(builder, vStatus.statusCode(), vStatus.message());
        return PartitionLag.createPartitionLag(builder, partitionId, lag, vStatusOffset);
    }

    public static PartitionLag createPartitionLag(short partitionId, int lag, VStatus vStatus){
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int partitionLagOffset = buildPartitionLag(builder, partitionId, lag, vStatus);
        builder.finish(partitionLagOffset);
        return PartitionLag.getRootAsPartitionLag(builder.dataBuffer());
    }

    public static int buildSubscriptionLag(FlatBufferBuilder builder, short topicId, short subscriptionId, List<PartitionLag> partitionLags){
        int[] partitionLagOffsets = partitionLags.stream()
            .map(partitionLag ->
                FlatbufUtils.buildPartitionLag(builder,
                    partitionLag.partitionId(),
                    partitionLag.lag(),
                    partitionLag.status()))
            .mapToInt(value -> value)
            .toArray();

        int partitionLagsVectorOffset = SubscriptionLag.createPartitionLagsVector(builder, partitionLagOffsets);
        return SubscriptionLag.createSubscriptionLag(builder, subscriptionId, topicId, partitionLagsVectorOffset);
    }

    public static SubscriptionLag createSubscriptionLag(short topicId, short subscriptionId, List<PartitionLag> partitionLags){
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int subscriptionLagOffset = buildSubscriptionLag(builder, topicId, subscriptionId, partitionLags);
        builder.finish(subscriptionLagOffset);
        return SubscriptionLag.getRootAsSubscriptionLag(builder.dataBuffer());
    }

    public static int buildVStatus(FlatBufferBuilder builder, short statusCode, String message){
        return VStatus.createVStatus(builder, statusCode, builder.createString(message));
    }

    public static VStatus createVStatus(short statusCode, String message){
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int vStatusOffset = buildVStatus(builder, statusCode, message);
        builder.finish(vStatusOffset);
        return VStatus.getRootAsVStatus(builder.dataBuffer());
    }
}
