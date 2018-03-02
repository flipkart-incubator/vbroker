package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.ProduceResponse;
import com.flipkart.vbroker.flatbuf.TopicPartitionProduceResponse;
import com.flipkart.vbroker.flatbuf.TopicProduceResponse;
import com.flipkart.vbroker.flatbuf.VResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ProduceResponseHandler implements ResponseHandler {

    public static ProduceResponse parse(VResponse vResponse) {
        ProduceResponse produceResponse = (ProduceResponse) vResponse.responseMessage(new ProduceResponse());
        assert produceResponse != null;
        return produceResponse;
    }

    @Override
    public void handle(VResponse vResponse) {
        ProduceResponse produceResponse = parse(vResponse);

        for (int i = 0; i < produceResponse.topicResponsesLength(); i++) {
            TopicProduceResponse topicProduceResponse = produceResponse.topicResponses(i);
            int topicId = topicProduceResponse.topicId();
            log.info("Handling ProduceResponse for topic {} with {} partition responses", topicId, topicProduceResponse.partitionResponsesLength());
            for (int j = 0; j < topicProduceResponse.partitionResponsesLength(); j++) {
                TopicPartitionProduceResponse partitionProduceResponse = topicProduceResponse.partitionResponses(j);
                //log.info("ProduceResponse for topic {} at partition {}", topicId, partitionProduceResponse);
                log.info("Response code for handling produceRequest for topic {} and partition {} is {}",
                    topicId, partitionProduceResponse.partitionId(), partitionProduceResponse.status().statusCode());
            }
        }
    }
}
