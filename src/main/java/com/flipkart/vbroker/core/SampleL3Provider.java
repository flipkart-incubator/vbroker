package com.flipkart.vbroker.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleL3Provider implements L3Provider {
    @Override
    public void write(String key, Object o) {
        log.info("Writing object {} to L3 with key {}", o.toString(), key);
    }
}
