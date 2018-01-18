package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.MessageSet;
import com.flipkart.vbroker.entities.ProduceRequest;
import com.flipkart.vbroker.services.ProducerService;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
@AllArgsConstructor
public class ProduceRequestHandler implements RequestHandler {

    private final ChannelHandlerContext ctx;
    private final ProduceRequest produceRequest;
    private final ProducerService producerService;

    @Override
    public void handle() {
        log.info("Getting messageSet for topic {} and partition {}", produceRequest.topicId(), produceRequest.partitionId());
        MessageSet messageSet = produceRequest.messageSet();
        for (int i = 0; i < messageSet.messagesLength(); i++) {
            Message message = messageSet.messages(i);
            ByteBuffer byteBuffer = message.bodyPayloadAsByteBuffer();
            ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
            log.info("Decoded msg with msgId: {} and payload: {}", message.messageId(),
                    Charsets.UTF_8.decode(byteBuffer).toString());

            producerService.produceMessage(message);
        }
    }
}
