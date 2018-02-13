package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.CallbackConfig;
import com.flipkart.vbroker.entities.HttpHeader;
import com.flipkart.vbroker.entities.HttpMethod;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.MessageConstants;
import com.flipkart.vbroker.entities.Subscription;
import com.flipkart.vbroker.entities.Topic;
import com.flipkart.vbroker.exceptions.SubscriptionNotFoundException;
import com.flipkart.vbroker.exceptions.TopicNotFoundException;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.subscribers.MessageWithGroup;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
public class HttpMessageProcessor implements MessageProcessor {

    private final AsyncHttpClient httpClient;
    private final TopicService topicService;
    private final SubscriptionService subscriptionService;
    private final ProducerService producerService;

    @Override
    public void process(MessageWithGroup messageWithGroup) throws Exception {
        Message message = messageWithGroup.getMessage();

        String httpUri = requireNonNull(message.httpUri());
        URI uri = new URI(httpUri);

        RequestBuilder requestBuilder = new RequestBuilder()
            .setUrl(requireNonNull(message.httpUri()))
            .setMethod(HttpMethod.name(message.httpMethod()))
            .setBody(message.bodyPayloadAsByteBuffer())
            .setCharset(StandardCharsets.UTF_8);

        requestBuilder.addHeader(MessageConstants.MESSAGE_ID_HEADER, message.messageId());
        requestBuilder.addHeader(MessageConstants.GROUP_ID_HEADER, message.groupId());
        requestBuilder.addHeader(MessageConstants.REQUEST_ID, UUID.randomUUID().toString());
        for (int i = 0; i < message.headersLength(); i++) {
            HttpHeader header = message.headers(i);
            requestBuilder.addHeader(header.key(), header.value());
        }

        Request request = requestBuilder.build();
        log.info("Making httpRequest to httpUri: {} and httpMethod: {}",
            httpUri,
            HttpMethod.name(message.httpMethod()));
        ListenableFuture<Response> reqFuture = httpClient.executeRequest(request);
        /*
         * not passing a thread pool to the reqFuture here
         * it will use the default IO thread where no one should block in our code
         */
        reqFuture.addListener(() -> {
            try {
                Response response = reqFuture.get();
                handleResponse(response, messageWithGroup);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Exception in executing request", e);
                messageWithGroup.sidelineGroup();
            } finally {
                messageWithGroup.forceUnlockGroup();
            }
        }, null);
    }

    private void handleResponse(Response response, MessageWithGroup messageWithGroup) {
        int statusCode = response.getStatusCode();
        if (statusCode >= 200 && statusCode < 300) {
            log.info("Response code is {}. Success in making httpRequest. Message processing now complete", statusCode);
            if (isCallbackRequired(statusCode, messageWithGroup.getMessage(), messageWithGroup.getTopicId(), messageWithGroup.subscriptionId())) {
                makeCallback(messageWithGroup.getMessage(), response);
            }
        } else if (statusCode >= 400 && statusCode < 500) {
            log.info("Response is 4xx. Sidelining the message");
            messageWithGroup.sidelineGroup();
        } else if (statusCode >= 500 && statusCode < 600) {
            log.info("Response is 5xx. Retrying the message");
            messageWithGroup.retry();
        }
    }

    /**
     * cases:
     * 1. Callback should be enabled in the config
     * 2. message shouldn't be a bridged message
     *
     * @param statusCode     after forward http call
     * @param message        being processed
     * @param subscriptionId to check callback for
     * @return if callback is required for the message
     */
    private boolean isCallbackRequired(int statusCode, Message message, short topicId, short subscriptionId) {
        String callbackCodes = null;
        Optional<String> isBridged = Optional.empty();
        for (int i = 0; i < message.headersLength(); i++) {
            HttpHeader header = message.headers(i);
            if (MessageConstants.BRIDGED.equalsIgnoreCase(header.key())) {
                isBridged = Optional.ofNullable(header.value());
            } else if (MessageConstants.CALLBACK_CODES.equalsIgnoreCase(header.key())) {
                callbackCodes = header.value();
            }
        }

        CallbackConfig callbackConfig = null;
        if (nonNull(callbackCodes)) {
            Optional<CallbackConfig> callbackConfigOpt = CallbackConfig.fromJson(callbackCodes);
            if (callbackConfigOpt.isPresent()) {
                callbackConfig = callbackConfigOpt.get();
            }
        } else {
            try {
                Subscription subscription = subscriptionService.getSubscription(topicId, subscriptionId);
                callbackConfig = CallbackConfig.getCallbackConfig(subscription.callbackConfig());
            } catch (SubscriptionNotFoundException e) {
                log.debug("Unable to find subscription with id {}. Assuming that this message is from a topic. Ignoring callback", subscriptionId);
            } catch (Exception ex) {
                log.error("Unable to get queue configuration for callback for subscription id {}. Ignoring.", subscriptionId, ex);
            }
        }

        return nonNull(callbackConfig) && callbackConfig.shouldCallback(statusCode)
            && !(isBridged.isPresent() && "Y".equalsIgnoreCase(isBridged.get()));
    }

    private void makeCallback(Message message, Response response) {
        if (message.callbackTopicId() > -1
            && message.callbackHttpMethod() > -1
            && !Strings.isNullOrEmpty(message.callbackHttpUri())) {
            log.info("Callback is enabled for this message {}", message.messageId());
            Message callbackMsg = MessageUtils.getCallbackMsg(message, response);
            try {
                Topic topic = topicService.getTopic(callbackMsg.topicId());
                log.info("Producing callback for message to {} queue", topic.topicName());
                producerService.produceMessage(topic, callbackMsg);
            } catch (TopicNotFoundException ex) {
                log.error("Topic with id {} not found to produce callback message. Dropping it");
            }
        }
    }
}
