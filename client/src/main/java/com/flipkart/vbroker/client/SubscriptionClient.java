package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 09/03/18.
 */

@Slf4j
@AllArgsConstructor
public class SubscriptionClient {
    private final NetworkClient networkClient;

    public CompletionStage<List<CreateSubscriptionResponse>> createSubscriptions(List<Subscription> subscriptions, Node node){
        List<ProtoSubscription> protoSubscriptions = subscriptions.stream()
            .map(subscription -> {
                try {
                    return ProtoSubscription.parseFrom(subscription.toBytes());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
        CreateSubscriptionsRequest createSubscriptionsRequest = CreateSubscriptionsRequest.newBuilder()
            .addAllSubscriptions(protoSubscriptions)
            .build();

        ProtoRequest protoRequest = ProtoRequest.newBuilder()
            .setCreateSubscriptionsRequest(createSubscriptionsRequest)
            .build();

        VRequest vRequest = FlatbufUtils.createVRequest((byte) 1, new Random().nextInt(), protoRequest);

        return networkClient.send(node, vRequest)
            .thenApply(response -> {
                ProtoResponse protoResponse = FlatbufUtils.getProtoResponse(response);
                assert protoResponse.hasCreateSubscriptionsResponse();
                return protoResponse.getCreateSubscriptionsResponse().getCreateSubscriptionResponsesList();
            });

    }
}
