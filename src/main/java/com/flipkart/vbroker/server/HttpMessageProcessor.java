package com.flipkart.vbroker.server;

import com.flipkart.vbroker.core.MessageWithGroup;
import com.flipkart.vbroker.core.SubscriberGroup;
import com.flipkart.vbroker.entities.HttpHeader;
import com.flipkart.vbroker.entities.HttpMethod;
import com.flipkart.vbroker.entities.Message;
import com.flipkart.vbroker.entities.MessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;

@Slf4j
public class HttpMessageProcessor implements MessageProcessor {

    private final AsyncHttpClient httpClient;

    public HttpMessageProcessor(AsyncHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void process(MessageWithGroup messageWithGroup) throws Exception {
        Message message = messageWithGroup.getMessage();
        log.info("Processing message with msg_id: {} and group_id: {}", message.messageId(), message.groupId());
        makeHttpRequest(message, messageWithGroup.getSubscriberGroup());
    }

    private void makeHttpRequest(Message message,
                                 SubscriberGroup subscriberGroup) throws URISyntaxException {
        String httpUri = requireNonNull(message.httpUri());
        URI uri = new URI(httpUri);

        RequestBuilder requestBuilder = new RequestBuilder()
                .setUrl(requireNonNull(message.httpUri()))
                .setMethod(HttpMethod.name(message.httpMethod()))
                .setBody(message.bodyPayloadAsByteBuffer())
                .setCharset(StandardCharsets.UTF_8);

        requestBuilder.addHeader(MessageConstants.MESSAGE_ID_HEADER, message.messageId());
        requestBuilder.addHeader(MessageConstants.GROUP_ID_HEADER, message.groupId());
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
                handleResponse(response);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Exception in executing request", e);
            } finally {
                log.info("Unlocking the subscriberGroup {}", subscriberGroup.getGroupId());
                subscriberGroup.unlock();
            }
        }, null);
    }

    private void handleResponse(Response response) {
        int statusCode = response.getStatusCode();
        if (statusCode >= 200 && statusCode < 300) {
            log.info("Response code is {}. Success in making httpRequest. Message processing now complete", statusCode);
        } else if (statusCode >= 400 && statusCode < 500) {
            log.info("Response is 4xx. Sidelining the message");
        } else if (statusCode >= 500 && statusCode < 600) {
            log.info("Response is 5xx. Retrying the message");
        }
    }
}