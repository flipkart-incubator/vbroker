package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.entities.RequestMessage;
import com.flipkart.vbroker.entities.VRequest;
import com.flipkart.vbroker.services.ProducerService;
import com.flipkart.vbroker.services.SubscriptionService;
import com.flipkart.vbroker.services.TopicService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * Created by kaushal.hooda on 19/02/18.
 */
public class RequestHandlerFactoryTest {

    ProducerService producerService;
    TopicService topicService;
    SubscriptionService subscriptionService;
    private RequestHandlerFactory requestHandlerFactory;
    private VRequest vRequest;


    @BeforeMethod
    public void setUp() {
        producerService = mock(ProducerService.class);
        topicService = mock(TopicService.class);
        subscriptionService = mock(SubscriptionService.class);
        requestHandlerFactory = new RequestHandlerFactory(producerService, topicService, subscriptionService);
        vRequest = mock(VRequest.class);
    }

    @Test
    public void shouldReturnGetLagsRequestHandlerForGetLagsRequest() {
        when(vRequest.requestMessageType()).thenReturn(RequestMessage.GetLagsRequest);
        RequestHandler handler = requestHandlerFactory.getRequestHandler(vRequest);
        assertTrue(handler instanceof GetLagsRequestHandler);
    }
}