package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.proto.*;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Created by kaushal.hooda on 09/03/18.
 */

@Slf4j
@AllArgsConstructor
public class SubscriptionClient {
    private final NetworkClient networkClient;
    private final Metadata metadata;

    public CompletionStage<List<CreateSubscriptionResponse>> createSubscriptions(List<Subscription> subscriptions) {
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

        VRequest vRequest = FlatbufUtils.createVRequest(protoRequest);

        return networkClient.send(getNode(), vRequest)
            .thenApply(response -> {
                ProtoResponse protoResponse = FlatbufUtils.getProtoResponse(response);
                assert protoResponse.hasCreateSubscriptionsResponse();
                return protoResponse.getCreateSubscriptionsResponse().getCreateSubscriptionResponsesList();
            });
    }

    public CompletionStage<List<GetSubscriptionResponse>> getSubscriptions(List<TopicSubscription> topicSubscriptions) {
        GetSubscriptionsRequest subscriptionsRequest = GetSubscriptionsRequest.newBuilder()
            .addAllSubscriptions(topicSubscriptions)
            .build();

        ProtoRequest protoRequest = ProtoRequest.newBuilder()
            .setGetSubscriptionsRequest(subscriptionsRequest)
            .build();

        VRequest vRequest = FlatbufUtils.createVRequest(protoRequest);

        return networkClient.send(getNode(), vRequest).thenApply(response -> {
            ProtoResponse protoResponse = FlatbufUtils.getProtoResponse(response);
            assert protoResponse.hasGetSubscriptionsResponse();
            return protoResponse.getGetSubscriptionsResponse().getSubscriptionResponsesList();
        });
    }

    private Node getNode() {
        return metadata.getClusterNodes().get(0);
    }
}
