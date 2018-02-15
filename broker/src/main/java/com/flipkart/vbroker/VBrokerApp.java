package com.flipkart.vbroker;

import com.flipkart.vbroker.server.VBrokerServer;
import lombok.extern.slf4j.Slf4j;

/*
 * @author vamsi, @date 2/1/18 2:55 PM
 */
@Slf4j
public class VBrokerApp {

    public static void main(String args[]) {
        log.info("== Starting VBrokerApp ==");

        VBrokerServer server = new VBrokerServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Got shutdown hook. Triggering stop and await termination of the server");
            server.stopAsync().awaitTerminated();
        }));

        server.startAsync().awaitRunning();
        log.info("VBrokerServer is now running");
        server.awaitTerminated();
        log.info("VBrokerServer is now terminated");

        log.info("== Shutting down VBrokerApp ==");
    }
}
