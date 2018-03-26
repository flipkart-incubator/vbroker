package com.flipkart.vbroker.utils;

import com.codahale.metrics.MetricRegistry;

public class MetricUtils {

    public static String brokerFullMetricName(String metricName) {
        return MetricRegistry.name("com.vbroker.broker", metricName);
    }

    public static String clientFullMetricName(String metricName) {
        return MetricRegistry.name("com.vbroker.client", metricName);
    }
}
