package com.flipkart.vbroker.server;

import com.flipkart.vbroker.entities.ProduceResponse;
import com.flipkart.vbroker.entities.TopicPartitionProduceResponse;
import com.flipkart.vbroker.entities.TopicProduceResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ProduceResponseHandler implements ResponseHandler {

    private final ProduceResponse produceResponse;

    @Override
    public void handle() {
        for (int i = 0; i < produceResponse.topicResponsesLength(); i++) {
            TopicProduceResponse topicProduceResponse = produceResponse.topicResponses(i);
            short topicId = topicProduceResponse.topicId();
            log.info("Handling ProduceResponse for topic {} with {} partition responses", topicId, topicProduceResponse.partitionResponsesLength());
            for (int j = 0; j < topicProduceResponse.partitionResponsesLength(); j++) {
                TopicPartitionProduceResponse partitionProduceResponse = topicProduceResponse.partitionResponses(j);
                //log.info("ProduceResponse for topic {} at partition {}", topicId, partitionProduceResponse);
                log.info("Response code for handling produceRequest for topic {} and partition {} is {}", topicId, partitionProduceResponse.partitionId(), partitionProduceResponse.statusCode());
            }
        }
    }
}
