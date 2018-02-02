package com.flipkart.vbroker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.Topic;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.utils.JsonUtils;
import org.omg.CORBA.ORB;

/**
 * Created by hooda on 2/2/18
 */

public class TopicMetadataService {
    private final TopicService topicService;

    private void saveTopicPartitionMetadata(TopicPartition partition){
        ObjectMapper mapper = JsonUtils.getObjectMapper();
        for(MessageGroup group : partition.getMessageGroups()){
            //what?
            // 1.save groupid only, let ioengine fetch messages by gID
            // 2.save nothing, ioengine fetches everything by partition
            // 3.save all msgids in group
            //OR: do nothing, let ioengine fetch all messages, and message groups are constructed on the fly?
        }
    }


    public TopicMetadataService(TopicService topicService) {
        this.topicService = topicService;
    }
}
