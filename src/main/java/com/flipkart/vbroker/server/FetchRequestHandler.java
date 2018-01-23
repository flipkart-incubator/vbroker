package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.ioengine.MessageService;
import com.flipkart.vbroker.protocol.Response;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
public class FetchRequestHandler implements RequestHandler {

    private final ChannelHandlerContext ctx;
    private final FetchRequest fetchRequest;
    private final MessageService messageService;

    @Override
    public void handle() {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        int noOfTopics = fetchRequest.topicRequestsLength();
        int[] topicFetchResponses = new int[noOfTopics];

        for (int i = 0; i < noOfTopics; i++) {
            TopicFetchRequest topicFetchRequest = fetchRequest.topicRequests(i);

            int noOfPartitions = topicFetchRequest.partitionRequestsLength();
            int[] partitionFetchResponses = new int[noOfPartitions];

            for (int j = 0; j < noOfPartitions; j++) {
                TopicPartitionFetchRequest topicPartitionFetchRequest = topicFetchRequest.partitionRequests(j);

                short noOfMessagesToFetch = topicPartitionFetchRequest.noOfMessages();
                log.info("Handling FetchRequest for {} messages for topic {} and partition {}",
                        noOfMessagesToFetch, topicFetchRequest.topicId(), topicPartitionFetchRequest.partitionId());

                int toFetchNoOfMessages = noOfMessagesToFetch;
                if (messageService.size() < noOfMessagesToFetch) {
                    toFetchNoOfMessages = messageService.size();
                }

                int[] messages = new int[toFetchNoOfMessages];
                int m = 0;
                while (messageService.hasNext() && m < noOfMessagesToFetch) {
                    log.info("No of messages in store are {}", messageService.size());
                    Message message = messageService.poll();

                    int headersVector = Message.createHeadersVector(builder, new int[0]);

                    ByteBuffer payloadByteBuffer = message.bodyPayloadAsByteBuffer();
                    byte[] messageBytes = new byte[payloadByteBuffer.remaining()];
                    payloadByteBuffer.get(messageBytes);

                    int messageOffset = Message.createMessage(
                            builder,
                            builder.createString(requireNonNull(message.messageId())),
                            builder.createString(requireNonNull(message.groupId())),
                            message.crc(),
                            message.version(),
                            message.seqNo(),
                            message.topicId(),
                            201,
                            builder.createString(requireNonNull(message.httpUri())),
                            message.httpMethod(),
                            message.callbackTopicId(),
                            builder.createString(requireNonNull(message.callbackHttpUri())),
                            message.callbackHttpMethod(),
                            headersVector,
                            messageBytes.length,
                            builder.createByteVector(messageBytes));
                    messages[m] = messageOffset;
                }

                int messagesVector = MessageSet.createMessagesVector(builder, messages);
                int messageSet = MessageSet.createMessageSet(builder, messagesVector);

                int topicPartitionFetchResponse = TopicPartitionFetchResponse.createTopicPartitionFetchResponse(
                        builder,
                        topicPartitionFetchRequest.partitionId(),
                        (short) 200,
                        messageSet);
                partitionFetchResponses[m] = topicPartitionFetchResponse;
            }

            int partitionResponsesVector = TopicFetchResponse.createPartitionResponsesVector(builder, partitionFetchResponses);
            int topicFetchResponse = TopicFetchResponse.createTopicFetchResponse(
                    builder,
                    topicFetchRequest.topicId(),
                    partitionResponsesVector);
            topicFetchResponses[i] = topicFetchResponse;
        }

        int topicResponsesVector = FetchResponse.createTopicResponsesVector(builder, topicFetchResponses);
        int fetchResponse = FetchResponse.createFetchResponse(builder, topicResponsesVector);

        int vResponse = VResponse.createVResponse(builder,
                1001,
                ResponseMessage.FetchResponse,
                fetchResponse);
        builder.finish(vResponse);
        ByteBuffer byteBuffer = builder.dataBuffer();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);

        Response response = new Response(byteBuf.readableBytes(), byteBuf);
        ctx.writeAndFlush(response);//.addListener(ChannelFutureListener.CLOSE);
    }
}
