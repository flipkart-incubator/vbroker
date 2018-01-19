package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.ioengine.MessageService;
import com.flipkart.vbroker.protocol.Response;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Iterator;

import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
public class FetchRequestHandler implements RequestHandler {

    private final ChannelHandlerContext ctx;
    private final FetchRequest fetchRequest;
    private final MessageService messageService;

    @Override
    public void handle() {
        short noOfMessagesToFetch = fetchRequest.noOfMessages();
        log.info("Handling FetchRequest for {} messages for topic {} and partition {}",
                noOfMessagesToFetch, fetchRequest.topicId(), fetchRequest.partitionId());

        FlatBufferBuilder builder = new FlatBufferBuilder();
        int[] messages = new int[noOfMessagesToFetch];

        Iterator<Message> messageIterator = messageService.messageIterator();
        int i = 0;
        while (messageIterator.hasNext() && i < noOfMessagesToFetch) {
            Message message = messageIterator.next();

            int headersVector = Message.createHeadersVector(builder, new int[0]);
            messages[i++] = Message.createMessage(
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
                    message.bodyPayloadLength(),
                    builder.createByteVector(new byte[]{}));
        }

        int messagesVector = MessageSet.createMessagesVector(builder, messages);
        int messageSet = MessageSet.createMessageSet(builder, messagesVector);
        int fetchResponse = FetchResponse.createFetchResponse(builder,
                fetchRequest.topicId(),
                fetchRequest.partitionId(),
                (short) 200,
                messageSet);
        int vResponse = VResponse.createVResponse(builder,
                1001,
                ResponseMessage.FetchResponse,
                fetchResponse);
        builder.finish(vResponse);
        ByteBuffer byteBuffer = builder.dataBuffer();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);

        Response response = new Response(byteBuf.readableBytes(), byteBuf);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
