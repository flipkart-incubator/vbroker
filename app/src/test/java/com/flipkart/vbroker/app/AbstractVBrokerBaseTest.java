package com.flipkart.vbroker.app;

import com.flipkart.vbroker.VBrokerConfig;
import com.flipkart.vbroker.client.MessageStore;
import com.flipkart.vbroker.client.ProducerRecord;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.server.VBrokerServer;
import com.flipkart.vbroker.utils.DummyEntities;
import com.flipkart.vbroker.wrappers.Topic;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.server.StubServer;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.util.HashMap;

import static com.flipkart.vbroker.app.MockHttp.*;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;

@Slf4j
public class AbstractVBrokerBaseTest {
    private static final Object shutdownTestSuiteLock = new Object();

    //CONSTANTS
    //broker port
    public static int BROKER_PORT;

    private static VBrokerServer server;
    private static boolean cleanedUpDone = false;
    protected StubServer httpServer;

    @AfterClass
    public static void tearDown() {
        synchronized (shutdownTestSuiteLock) {
            if (!cleanedUpDone) {
                log.info("Shutting down VBroker test suite");
                server.stopAsync().awaitTerminated();
                cleanedUpDone = true;
                log.info("Test suite cleanup done");
            }
        }
    }

    @BeforeClass
    public void setUp() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Handled Shutdown SIGNAL. Shutting down the test suite safely");
            tearDown();
            log.info("Shutdown done safely");
        }));

        log.info("Starting VBroker test suite");

        String configFile = "test-broker.properties";
        VBrokerConfig config = VBrokerConfig.newConfig(configFile);

        BROKER_PORT = config.getBrokerPort();
        server = new VBrokerServer(config);
        server.startAsync().awaitRunning();
    }

    @BeforeMethod
    public void beforeTest() {
        server.awaitRunning();

        httpServer = new StubServer(MOCK_HTTP_SERVER_PORT).run();

        whenHttp(httpServer).match(post(MockURI.URI_200)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_204)).then(status(HttpStatus.NO_CONTENT_204), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_400)).then(status(HttpStatus.BAD_REQUEST_400), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(MockURI.URI_400)).then(status(HttpStatus.BAD_REQUEST_400), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_404)).then(status(HttpStatus.NOT_FOUND_404), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(MockURI.URI_404)).then(status(HttpStatus.NOT_FOUND_404), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_429)).then(status(HttpStatus.getHttpStatus(429)), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_500)).then(status(HttpStatus.INTERNAL_SERVER_ERROR_500), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(MockURI.URI_500)).then(status(HttpStatus.INTERNAL_SERVER_ERROR_500), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_504)).then(status(HttpStatus.GATEWAY_TIMEOUT_504), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(patch(MockURI.URI_200)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(post(MockURI.URI_MOCK_APP)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        whenHttp(httpServer).match(put(MockURI.URI_MOCK_APP2)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY));
        //Sleep for 5 seconds
        whenHttp(httpServer).match(post(MockURI.SLEEP_200)).then(status(HttpStatus.OK_200), stringContent(MOCK_RESPONSE_BODY), sleep(6000));
        whenHttp(httpServer).match(post(MockURI.SLEEP_404)).then(status(HttpStatus.NOT_FOUND_404), stringContent(MOCK_RESPONSE_BODY), sleep(1000));
    }

    protected void clearHttpCalls() {
        if (httpServer != null) {
            //httpServer.clear();
            httpServer.clearCalls();
        }
    }

    @AfterMethod
    public void afterTest() {
        //cleanup http calls to the stub server
        clearHttpCalls();
        httpServer.stop();
    }

    private Action sleep(long sleepTime) {
        return Action.delay((int) sleepTime);
    }

    public Topic createGroupedTopic() {
        return DummyEntities.groupedTopic;
    }

    public Topic createUnGroupedTopic() {
        return DummyEntities.unGroupedTopic;
    }
}
