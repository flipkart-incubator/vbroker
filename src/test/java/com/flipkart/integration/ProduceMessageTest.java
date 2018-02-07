package com.flipkart.integration;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.VBrokerClient;
import com.flipkart.vbroker.server.VBrokerServer;
import com.flipkart.vbroker.services.CuratorService;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
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
    StubServer consumer;

    @BeforeSuite
    public void startServer() throws IOException {
        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");

        Thread t = new Thread(new Runnable() {
            public void run() {
                CuratorService curatorService;
                try {
                    curatorService = new CuratorService(config);
                    server = new VBrokerServer(config, curatorService);
                    server.run();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        t.start();
        consumer = new StubServer(MOCK_CONSUMER_PORT).run();

    }

    @Test
    public void shouldProduceAndConsumeMessage() throws IOException, InterruptedException {
        whenHttp(consumer).match(Condition.post("/messages")).then(status(HttpStatus.OK_200));

        //Send produce request
        VBrokerClient.main(new String[0]);

        //Verify the message is consumed
        verifyHttp(consumer).once(method(Method.POST),
                uri("/messages"));
    }

    @AfterSuite
    public void stopServer() {
        consumer.stop();
    }
}
