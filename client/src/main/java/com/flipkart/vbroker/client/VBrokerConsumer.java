package com.flipkart.vbroker.client;

import com.codahale.metrics.MetricRegistry;
import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.flatbuf.*;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.utils.RandomUtils;
import com.flipkart.vbroker.wrappers.Subscription;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.flatbuffers.FlatBufferBuilder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class VBrokerConsumer implements Consumer {
    private final NetworkClient networkClient;
    private final Metadata metadata;
    private final List<Subscription> subscriptions = Lists.newArrayList();

    public VBrokerConsumer(NetworkClient networkClient,
                           Metadata metadata) {
        this.networkClient = networkClient;
        this.metadata = metadata;
    }

    public VBrokerConsumer(VBClientConfig config, MetricRegistry metricRegistry) {
        this(new NetworkClientImpl(metricRegistry), new MetadataImpl(config));
    }

    @Override
    public void subscribe(Set<Subscription> subscriptions) {
        this.subscriptions.addAll(subscriptions);
    }

    @Override
    public void unSubscribe(Set<Subscription> subscriptions) {
        this.subscriptions.removeAll(subscriptions);
    }

    @Override
    public CompletionStage<List<ConsumerRecord>> poll(int maxRecords, int timeoutMs) {
        FlatBufferBuilder builder = FlatbufUtils.newBuilder();

        //copying the subscriptions for each poll() as we don't want an update to subscriptions affecting the current poll()
        //essentially making it immutable
        List<Subscription> currSubscriptions = Lists.newArrayList();
        currSubscriptions.addAll(subscriptions);

        int[] topicFetchRequests = new int[currSubscriptions.size()];
        for (int s = 0; s < currSubscriptions.size(); s++) {
            Subscription subscription = currSubscriptions.get(s);
            List<PartSubscription> partSubscriptions = metadata.getPartSubscriptions(subscription.topicId(), subscription.id());

            int[] tpFetchRequests = new int[partSubscriptions.size()];
            for (int ps = 0; ps < partSubscriptions.size(); ps++) {
                int topicPartitionFetchRequest = TopicPartitionFetchRequest.createTopicPartitionFetchRequest(
                    builder,
                    partSubscriptions.get(ps).getId(),
                    maxRecords,
                    ProtoQType.SIDELINE,
                    timeoutMs);
                tpFetchRequests[ps] = topicPartitionFetchRequest;
            }

            int partitionRequestsVector = TopicFetchRequest.createPartitionRequestsVector(builder, tpFetchRequests);
            int topicFetchRequest = TopicFetchRequest.createTopicFetchRequest(builder,
                subscription.id(),
                subscription.topicId(),
                partitionRequestsVector);
            topicFetchRequests[s] = topicFetchRequest;
        }

        int topicRequestsVector = FetchRequest.createTopicRequestsVector(builder, topicFetchRequests);

        int fetchRequest = FetchRequest.createFetchRequest(builder, topicRequestsVector);
        int vRequestPos = VRequest.createVRequest(builder,
            (byte) 1,
            RandomUtils.generateRandomCorrelationId(),
            RequestMessage.FetchRequest,
            fetchRequest);
        builder.finish(vRequestPos);
        ByteBuffer byteBuffer = builder.dataBuffer();
        VRequest vRequest = VRequest.getRootAsVRequest(byteBuffer);

        return networkClient.send(getNode(), vRequest)
            .thenApply(this::parseResponse);
    }

    private Node getNode() {
        return metadata.getClusterNodes().get(0);
    }

    private List<ConsumerRecord> parseResponse(VResponse vResponse) {
        FetchResponse fetchResponse = (FetchResponse) vResponse.responseMessage(new FetchResponse());
        assert fetchResponse != null;

        return IntStream.range(0, fetchResponse.topicResponsesLength())
            .mapToObj(fetchResponse::topicResponses)
            .flatMap(topicFetchResponse -> parseTopicFetchResponse(topicFetchResponse).stream())
            .collect(Collectors.toList());
    }

    private List<ConsumerRecord> parseTopicFetchResponse(TopicFetchResponse topicFetchResponse) {
        return IntStream.range(0, topicFetchResponse.partitionResponsesLength())
            .mapToObj(topicFetchResponse::partitionResponses)
            .flatMap(topicPartitionFetchResponse ->
                parseTopicPartFetchResponse(topicPartitionFetchResponse, topicFetchResponse.topicId())
                    .stream())
            .collect(Collectors.toList());
    }

    private List<ConsumerRecord> parseTopicPartFetchResponse(TopicPartitionFetchResponse topicPartitionFetchResponse,
                                                             int topicId) {
        List<ConsumerRecord> records = new ArrayList<>();
        VStatus status = topicPartitionFetchResponse.status();
        log.info("FetchResponse status for topic {} and partition {} is {}",
            topicId, topicPartitionFetchResponse.partitionId(), status.statusCode());
        if (StatusCode.ConsumeSuccess_NoError == status.statusCode()) {
            MessageSet messageSet = topicPartitionFetchResponse.messageSet();
            int noOfMessages = messageSet.messagesLength();
            log.info("Handling FetchResponse for topic {} and partition {} having {} messages",
                topicId, topicPartitionFetchResponse.partitionId(), noOfMessages);
            List<ConsumerRecord> consumerRecords = IntStream.range(0, noOfMessages)
                .mapToObj(messageSet::messages)
                .map(message -> {
                    ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
                    log.info("Decoded msg with msgId: {} and payload: {}", message.messageId(),
                        Charsets.UTF_8.decode(byteBuffer).toString());
                    return RecordUtils.newConsumerRecord(message);
                }).collect(Collectors.toList());
            records.addAll(consumerRecords);
        }

        return records;
    }

    @Override
    public CompletionStage<Void> commitOffset(String group, int offset) {
        return null;
    }

    @Override
    public CompletionStage<Integer> getOffset(String group) {
        return null;
    }

    @Override
    public void close() throws Exception {
        networkClient.close();
        log.info("VBrokerConsumer closed");
    }
}
