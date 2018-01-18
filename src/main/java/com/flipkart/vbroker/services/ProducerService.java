package com.flipkart.vbroker.services;

import com.flipkart.vbroker.entities.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ProducerService {

    public void produceMessage(Message message) {
        log.info("Producing message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
    }
}
