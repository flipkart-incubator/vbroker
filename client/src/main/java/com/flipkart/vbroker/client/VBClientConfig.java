package com.flipkart.vbroker.client;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
@ToString
public class VBClientConfig {
    private final Properties properties;

    //broker
    private String brokerHost;
    private int brokerPort;

    //producer
    private int maxAccumulatorRecords;
    private int maxBatchSizeBytes;
    private int maxBatchRecords;
    private long lingerTimeMs;

    //metadata
    private long metadataExpiryTimeMs;

    public VBClientConfig(Properties props) {
        this.properties = props;
        reloadConfigs();
    }

    public static VBClientConfig newConfig(String propertiesFile) throws IOException {
        Properties properties = new Properties();
        ByteSource byteSource = Resources.asByteSource(Resources.getResource(propertiesFile));
        try (InputStream inputStream = byteSource.openBufferedStream()) {
            properties.load(inputStream);
        }
        return new VBClientConfig(properties);
    }

    private void reloadConfigs() {
        this.brokerHost = properties.getProperty("broker.host");
        this.brokerPort = Ints.tryParse(properties.getProperty("broker.port"));

        this.maxBatchSizeBytes = Ints.tryParse(properties.getProperty("max.batch.size.bytes"));
        this.maxBatchRecords = Ints.tryParse(properties.getProperty("max.batch.records"));
        this.lingerTimeMs = Longs.tryParse(properties.getProperty("linger.time.ms"));
        this.maxAccumulatorRecords = Ints.tryParse(properties.getProperty("max.accumulator.records"));

        this.metadataExpiryTimeMs = Longs.tryParse(properties.getProperty("metadata.expiry.time.ms"));
    }
}
