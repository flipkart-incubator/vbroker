package com.flipkart.vbroker.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.flipkart.vbroker.utils.JsonUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * Created by hooda on 19/1/18
 */

@Getter
@EqualsAndHashCode(exclude = "partSubscriptions")
public class Subscription {
    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();

    private final short id;
    private final String name;
    private final Topic topic;
    private final List<PartSubscription> partSubscriptions;
    private final boolean grouped ;

    public Subscription(short id, String name, Topic topic, List<PartSubscription> partSubscriptions, boolean grouped) {
        this.id = id;
        this.name = name;
        this.topic = topic;
        this.partSubscriptions = partSubscriptions;
        this.grouped = grouped;
    }

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
        private boolean grouped ;

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

        public Subscription build() {
            return new Subscription(id, name, topic, partSubscriptions, grouped);
        }
    }
}