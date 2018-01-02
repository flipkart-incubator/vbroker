package com.flipkart.vbroker;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/*
 * @author vamsi, @date 2/1/18 2:55 PM
 */
@Slf4j
public class VBrokerApp {

    public static void main(String args[]) throws IOException {
        VBrokerConfig config = VBrokerConfig.newConfig("broker.properties");

        log.info("Configs: ", config);
        log.info("Hello, World VBroker!");
    }
}
