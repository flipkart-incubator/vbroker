package com.flipkart.vbroker;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.server.VBrokerServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/*
 * @author vamsi, @date 2/1/18 2:55 PM
 */
@Slf4j
public class VBrokerApp {

    public static void main(String args[]) throws IOException {
        log.info("== Starting VBrokerApp ==");

        MetricRegistry metricRegistry = new MetricRegistry();

        log.info("Starting JmxReporter for metrics");
        JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).build();
        reporter.start();

        if (args.length < 1) {
            throw new VBrokerException("Please specify the properties file as the first argument");
        }

        String configFile = "broker.properties";
        VBrokerConfig config = VBrokerConfig.newConfig(configFile);
        log.info("Configs: {}", config);

        VBrokerServer server = new VBrokerServer(config, metricRegistry);

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
