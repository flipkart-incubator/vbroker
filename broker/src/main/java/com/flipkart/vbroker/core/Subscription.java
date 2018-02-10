package com.flipkart.vbroker.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.flipkart.vbroker.subscribers.PartSubscription;
import com.flipkart.vbroker.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * Created by hooda on 19/1/18
 */

@Getter
@EqualsAndHashCode(exclude = "partSubscriptions")
@AllArgsConstructor
public class Subscription {
    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();

    private final short id;
    private final String name;
    private final Topic topic;
    @JsonIgnore
    private final List<PartSubscription> partSubscriptions;
    private final boolean grouped;
    private final CallbackConfig callbackConfig;

//    public Subscription(short id, String name, Topic topic, List<PartSubscription> partSubscriptions, boolean grouped) {
//        this.id = id;
//        this.name = name;
//        this.topic = topic;
//        this.partSubscriptions = partSubscriptions;
//        this.grouped = grouped;
//    }

    public void addPartSubscription(PartSubscription partSubscription) {
        this.partSubscriptions.add(partSubscription);
    }

    public PartSubscription getPartSubscription(int partSubscriptionId) {
        return partSubscriptions.get(partSubscriptionId);
    }

    public String toJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }

    public byte[] toBytes() throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(this);
    }

    @JsonPOJOBuilder
    public static final class SubscriptionBuilder {
        private Topic topic;
        private short id;
        private String name;
        private List<PartSubscription> partSubscriptions;
        private boolean grouped;
        private CallbackConfig callbackConfig;

        private SubscriptionBuilder() {
        }

        public static SubscriptionBuilder aSubscription() {
            return new SubscriptionBuilder();
        }

        public SubscriptionBuilder withId(short id) {
            this.id = id;
            return this;
        }

        public SubscriptionBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public SubscriptionBuilder withTopic(Topic topic) {
            this.topic = topic;
            return this;
        }

        public SubscriptionBuilder withPartSubscriptions(List<PartSubscription> partSubscriptions) {
            this.partSubscriptions = partSubscriptions;
            return this;
        }

        public SubscriptionBuilder withGrouped(boolean grouped) {
            this.grouped = grouped;
            return this;
        }

        public SubscriptionBuilder withCallbackConfig(CallbackConfig callbackConfig) {
            this.callbackConfig = callbackConfig;
            return this;
        }

        public Subscription build() {
            return new Subscription(id, name, topic, partSubscriptions, grouped, callbackConfig);
        }
    }
}