package com.flipkart.vbroker.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.flipkart.vbroker.utils.JsonUtils;
import lombok.Getter;

/**
 * Created by hooda on 19/1/18
 */
@Getter
public class Queue {
    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();

    private final Topic topic;
    private final Subscription subscription;

    public Queue(Topic topic, Subscription subscription) {
        this.topic = topic;
        this.subscription = subscription;
    }

    public String toJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }

    public byte[] toBytes() throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(this);
    }


    @JsonPOJOBuilder
    public static final class QueueBuilder {
        private Topic topic;
        private Subscription subscription;

        private QueueBuilder() {
        }

        public static QueueBuilder aQueue() {
            return new QueueBuilder();
        }

        public QueueBuilder withTopic(Topic topic) {
            this.topic = topic;
            return this;
        }

        public QueueBuilder withSubscription(Subscription subscription) {
            this.subscription = subscription;
            return this;
        }

        public Queue build() {
            Queue queue = new Queue(topic, subscription);
            return queue;
        }
    }
}
