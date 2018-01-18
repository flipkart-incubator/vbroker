package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.FetchResponse;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.MessageSet;
import com.google.common.base.Charsets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
@AllArgsConstructor
public class FetchResponseHandler implements ResponseHandler {

    private final FetchResponse fetchResponse;

    @Override
    public void handle() {
        log.info("Handling fetchResponse for topic {} and partition {}", fetchResponse.topicId(), fetchResponse.partitionId());
        MessageSet messageSet = fetchResponse.messageSet();
        for (int i = 0; i < messageSet.messagesLength(); i++) {
            Message message = messageSet.messages(i);
            ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
            //ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
            log.info("Decoded msg with msgId: {} and payload: {}", message.messageId(),
                    Charsets.UTF_8.decode(byteBuffer).toString());
        }
    }
}
