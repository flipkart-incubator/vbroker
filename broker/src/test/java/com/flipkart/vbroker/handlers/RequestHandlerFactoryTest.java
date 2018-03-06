package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.RequestMessage;
import com.flipkart.vbroker.flatbuf.VRequest;
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
        requestHandlerFactory = new RequestHandlerFactory(producerService, topicService, subscriptionService, null, null);
        vRequest = mock(VRequest.class);
    }

    @Test
    public void shouldReturnGetLagsRequestHandlerForGetLagsRequest() {
        when(vRequest.requestMessageType()).thenReturn(RequestMessage.ControlRequest);
        //TODO
        //Will fail. Don't know how to mock FlatbufUtils since it's not passed anywhere.
        RequestHandler handler = requestHandlerFactory.getRequestHandler(vRequest);
        assertTrue(handler instanceof GetSubscriptionLagsRequestHandler);
    }
}