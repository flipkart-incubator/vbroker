package com.flipkart.vbroker.server;

import com.flipkart.vbroker.MessageConstants;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.HttpHeader;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.subscribers.QType;
import com.google.common.primitives.Ints;
import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.curator.shaded.com.google.common.collect.Sets;
import org.asynchttpclient.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.flipkart.vbroker.subscribers.QType.*;
import static java.util.Objects.requireNonNull;

public class MessageUtils {

    public static QType getNextRetryQType(QType qType) {
        QType destinationQType;
        switch (qType) {
            case MAIN:
                destinationQType = RETRY_1;
                break;
            case RETRY_1:
                destinationQType = RETRY_2;
                break;
            case RETRY_2:
                destinationQType = RETRY_3;
                break;
            case RETRY_3:
                destinationQType = QType.SIDELINE;
                break;
            default:
                throw new VBrokerException("Unknown QType: " + qType);
        }
        return destinationQType;
    }

    public static Message getCallbackMsg(Message message, Response response) {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        int messageId = builder.createString("cbk_" + requireNonNull(message.messageId()));
        int groupId = builder.createString(requireNonNull(message.groupId()));
        byte crc = message.crc();
        byte version = message.version();
        int seqNo = message.seqNo();
        short partitionId = 0;

        int topicId = message.callbackTopicId();
        int httpUri = builder.createString(requireNonNull(message.callbackHttpUri()));
        int httpMethod = message.callbackHttpMethod();

        short callbackTopicId = -1;
        int callbackHttpUri = builder.createString("dummy");
        int callbackHttpMethod = message.callbackHttpMethod();

        Set<String> toRemoveHeaders = Sets.newHashSet(
            MessageConstants.BRIDGED_COUNT,
            MessageConstants.REPLY_TO_HEADER,
            MessageConstants.REPLY_TO_HTTP_URI_HEADER,
            MessageConstants.REPLY_TO_HTTP_METHOD_HEADER);

        List<Integer> headersList = new ArrayList<>();
        //int[] headers = new int[message.headersLength()];
        for (int i = 0; i < message.headersLength(); i++) {
            HttpHeader header = message.headers(i);
            if (!toRemoveHeaders.contains(header.key())) {
                int headerOffset = getHeader(builder, requireNonNull(header.key()), requireNonNull(header.value()));
                headersList.add(headerOffset);
            }
        }
        headersList.add(getHeader(builder, MessageConstants.ACK, "Y"));
        headersList.add(getHeader(builder,
            MessageConstants.DESTINATION_RESPONSE_STATUS,
            String.valueOf(response.getStatusCode())));
        headersList.add(getHeader(builder, MessageConstants.CORRELATION_ID_HEADER, message.messageId()));

        //TODO: pending things
        //message.addHeader(MessageConstants.PRODUCER_APP_ID, message.getHeader(MessageConstants.SUBSCRIPTION_APP_ID));
        //message.setReplyTo(null);
        //message.setReplyToHttpMethod(null);
        //message.setReplyToHttpUri(null);
        //message.setCorrelationId(message.getMessageId());
        //message.setDestinationResponseStatus(response.getStatusCode());
        //message.setHttpResponseCode(0);
        //message.setHttpResponseBody(null);

        int[] headers = Ints.toArray(headersList);
        int headersVector = Message.createHeadersVector(builder, headers);

        byte[] payload = response.getResponseBodyAsBytes();
        int messageOffset = Message.createMessage(
            builder,
            messageId,
            groupId,
            crc,
            version,
            seqNo,
            topicId,
            partitionId,
            message.attributes(),
            httpUri,
            httpMethod,
            callbackTopicId,
            callbackHttpUri,
            callbackHttpMethod,
            headersVector,
            payload.length,
            builder.createByteVector(payload)
        );

        builder.finish(messageOffset);
        return Message.getRootAsMessage(builder.dataBuffer());
    }

    private static int getHeader(FlatBufferBuilder builder, String key, String value) {
        return HttpHeader.createHttpHeader(builder,
            builder.createString(key),
            builder.createString(value));
    }
}
