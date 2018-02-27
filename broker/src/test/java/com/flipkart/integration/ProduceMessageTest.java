package com.flipkart.integration;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.VBrokerClient;
import com.flipkart.vbroker.server.VBrokerServer;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.uri;

/**
 * Created by kaushal.hooda on 16/01/18.
 */
public class ProduceMessageTest {
    public static final int MOCK_CONSUMER_PORT = 15000;

    VBrokerServer server;
    StubServer stubServer;

    @BeforeClass
    public void setUp() throws IOException {
        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");
        server = new VBrokerServer();
        server.startAsync().awaitRunning();
        stubServer = new StubServer(MOCK_CONSUMER_PORT).run();
    }

    @AfterClass
    public void tearDown() {
        stubServer.stop();
        server.stopAsync().awaitTerminated();
    }

    @Test
    public void shouldProduceAndConsumeMessage() throws IOException, InterruptedException {

        whenHttp(stubServer).match(Condition.post("/messages")).then(status(HttpStatus.OK_200));

        //Send produce request
        VBrokerClient.main(new String[0]);

        //Verify the message is consumed
        verifyHttp(stubServer).once(method(Method.POST),
            uri("/messages"));
    }

    @AfterSuite
    public void stopServer() {
        stubServer.stop();
    }
}
