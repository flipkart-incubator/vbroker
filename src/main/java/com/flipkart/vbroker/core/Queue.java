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
    public static final CallbackConfig DEFAULT_CALLBACK_CONFIG = new CallbackConfig();
    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();

    static {
        DEFAULT_CALLBACK_CONFIG.addRange(new CallbackConfig.CodeRange(200, 299));
    }

    private final Topic topic;
    private final Subscription subscription;
    private final CallbackConfig callbackConfig;

    public Queue(Topic topic,
                 Subscription subscription,
                 CallbackConfig callbackConfig) {
        this.topic = topic;
        this.subscription = subscription;
        this.callbackConfig = callbackConfig;
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
        private CallbackConfig callbackConfig;

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

        public QueueBuilder withCallbackConfig(CallbackConfig callbackConfig) {
            this.callbackConfig = callbackConfig;
            return this;
        }

        public Queue build() {
            return new Queue(topic, subscription, callbackConfig);
        }
    }
}
