package com.flipkart.vbroker.server;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.CallbackConfig;
import com.flipkart.vbroker.core.TopicPartMessage;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.SubPartDataManager;
import com.flipkart.vbroker.entities.*;
import com.flipkart.vbroker.exceptions.CallbackProducingFailedException;
import com.flipkart.vbroker.exceptions.TopicNotFoundException;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import com.flipkart.vbroker.subscribers.IterableMessage;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
public class HttpMessageProcessor implements MessageProcessor {

    private final AsyncHttpClient httpClient;
    private final TopicService topicService;
    private final SubscriptionService subscriptionService;
    private final ProducerService producerService;
    private final SubPartDataManager subPartDataManager;

    @Override
    public void process(IterableMessage iterableMessage) throws Exception {
        Message message = iterableMessage.getMessage();

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
                Response response = reqFuture.get(5, TimeUnit.SECONDS);
                handleResponse(response, iterableMessage);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Exception in executing request", e);
                //subPartDataManager.sideline(iterableMessage)
                sideline(iterableMessage);
            } catch (TimeoutException e) {
                log.error("Timed out while making the http request", e);
                retry(iterableMessage);
            } finally {
                iterableMessage.unlock();
            }
        }, null);
    }

    private void handleResponse(Response response, IterableMessage iterableMessage) {
        int statusCode = response.getStatusCode();
        if (statusCode >= 200 && statusCode < 300) {
            log.info("Response code is {}. Success in making httpRequest. Message processing now complete", statusCode);
        } else if (statusCode >= 400 && statusCode < 500) {
            log.info("Response is 4xx. Sidelining the message");
            sideline(iterableMessage);
        } else if (statusCode >= 500 && statusCode < 600) {
            log.info("Response is 5xx. Retrying the message");
            retry(iterableMessage);
        }

        handleCallback(response, iterableMessage, statusCode);
    }

    private void handleCallback(Response response, IterableMessage iterableMessage, int statusCode) {
        CompletionStage<Subscription> subscriptionStage = subscriptionService
            .getSubscription(iterableMessage.getTopicId(), iterableMessage.subscriptionId());
        subscriptionStage.thenAcceptAsync(subscription -> {
            if (isCallbackRequired(statusCode, iterableMessage.getMessage(), subscription)) {
                log.info("Callback is enabled for this message {}", iterableMessage.getMessage().messageId());
                makeCallback(iterableMessage, response)
                    .handleAsync(((messageMetadata, throwable) -> {
                        if (nonNull(throwable)) {
                            try {
                                //TODO: ensure that you use a separate thread (like default fork-join Java pool)
                                // for this or else this will get blocked
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                log.error("Interrupted sleep", e);
                            }
                            return makeCallback(iterableMessage, response);
                        }
                        return null;
                    }));
            }
        });
    }

    private CompletionStage<Void> sideline(IterableMessage iterableMessage) {
        return subPartDataManager.sideline(iterableMessage.getPartSubscription(), iterableMessage);
    }

    private CompletionStage<Void> retry(IterableMessage iterableMessage) {
        return subPartDataManager.retry(iterableMessage.getPartSubscription(), iterableMessage);
    }

    /**
     * cases:
     * 1. Callback should be enabled in the config
     * 2. message shouldn't be a bridged message
     *
     * @param statusCode   after forward http call
     * @param message      being processed
     * @param subscription to check callback for
     * @return if callback is required for the message
     */
    private boolean isCallbackRequired(int statusCode, Message message, Subscription subscription) {
        boolean isCallbackEnabled =
            message.callbackTopicId() > -1 &&
                message.callbackHttpMethod() > -1 &&
                !Strings.isNullOrEmpty(message.callbackHttpUri());
        if (!isCallbackEnabled) return false; //optimisation

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
            callbackConfig = CallbackConfig.getCallbackConfig(subscription.callbackConfig());
        }

        return nonNull(callbackConfig) && callbackConfig.shouldCallback(statusCode)
            && !(isBridged.isPresent() && "Y".equalsIgnoreCase(isBridged.get()));
    }

    private CompletionStage<MessageMetadata> makeCallback(IterableMessage iterableMessage, Response response) {
        Message callbackMsg = MessageUtils.getCallbackMsg(iterableMessage.getMessage(), response);
        return topicService
            .getTopic(callbackMsg.topicId())
            .thenComposeAsync(topic -> {
                log.info("Producing callback for message to {} queue", topic.id());
                TopicPartition topicPartition = new TopicPartition(callbackMsg.partitionId(), topic.id(), topic.grouped());
                TopicPartMessage topicPartMessage =
                    TopicPartMessage.newInstance(topicPartition, callbackMsg);
                return producerService.produceMessage(topicPartMessage);
            }).exceptionally((Function<Throwable, MessageMetadata>) input -> {
                if (input instanceof TopicNotFoundException) {
                    log.error("Topic with id {} not found to produce callback message. Dropping it");
                    return null;
                } else {
                    throw new CallbackProducingFailedException("Unable to produce callback message. Please retry again");
                }
            });
    }
}
