package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.Message;
import com.flipkart.vbroker.iterators.DataIterator;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RFuture;
import org.redisson.api.RList;
import org.redisson.api.RListAsync;
import org.redisson.api.RedissonClient;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletionStage;

@Slf4j
public class RedisUnGroupedTopicPartData extends RedisTopicPartData implements TopicPartData {
    private final RedissonClient client;
    private final TopicPartition topicPartition;

    public RedisUnGroupedTopicPartData(RedissonClient client,
                                       TopicPartition topicPartition) {
        this.client = client;
        this.topicPartition = topicPartition;
    }

    @Override
    public CompletionStage<MessageMetadata> addMessage(Message message) {
        Message messageBuffer = Message.getRootAsMessage(super.buildMessage(message));
        RedisObject rObjMessage = new RedisMessageObject(messageBuffer);
        RListAsync<RedisObject> topicPartitionList = client.getList(topicPartition.toString());
        RFuture<Boolean> topicPartitionAddFuture = topicPartitionList.addAsync(rObjMessage);
        return topicPartitionAddFuture.thenApplyAsync(result -> {
            if (result) {
                return new MessageMetadata(
                    message.messageId(),
                    message.topicId(),
                    message.partitionId(),
                    new Random().nextInt());
            } else {
                throw new VBrokerException("Unable to add message to redis : adding to topicPartitionList or messageGroupList failed");
            }
        }).exceptionally(exception -> {
            throw new VBrokerException("Unable to add message to redis : " + exception.getMessage());
        });
    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {
        throw new UnsupportedOperationException("For an un-grouped queue, you cannot list unique groups");
    }

    @Override
    public DataIterator<Message> iteratorFrom(String group, int seqNoFrom) {
        throw new UnsupportedOperationException("For an un-grouped queue, you cannot have a group level iterator");
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset(String group) {
        throw new UnsupportedOperationException("Group level offset is not defined for ungrouped partition");
    }

    @Override
    public DataIterator<Message> iteratorFrom(int seqNoFrom) {
        RList<RedisObject> rList = client.getList(topicPartition.toString());
        return super.iteratorFrom(rList, "un_grouped", seqNoFrom);
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset() {
        RListAsync<RedisObject> rListAsync = client.getList(topicPartition.toString());
        return rListAsync.sizeAsync();
    }
}
