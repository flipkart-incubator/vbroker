package com.flipkart.vbroker.data.redis;

import com.flipkart.vbroker.client.MessageMetadata;
import com.flipkart.vbroker.core.MessageGroup;
import com.flipkart.vbroker.core.TopicPartition;
import com.flipkart.vbroker.data.TopicPartData;
import com.flipkart.vbroker.exceptions.VBrokerException;
import com.flipkart.vbroker.flatbuf.Message;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Slf4j
public class RedisGroupedTopicPartData extends RedisTopicPartData implements TopicPartData {

    private static RedissonClient client;
    private TopicPartition topicPartition;

    public RedisGroupedTopicPartData(RedissonClient client,
                                     TopicPartition topicPartition) {
        this.client = client;
        this.topicPartition = topicPartition;
    }

    @Override
    public CompletionStage<MessageMetadata> addMessage(Message message) {

        Message messageBuffer = Message.getRootAsMessage(super.buildMessage(message));
        MessageGroup messageGroup = new MessageGroup(messageBuffer.groupId(), topicPartition);

        RedisObject rObjMessage = new RedisMessageObject(messageBuffer);
        RedisObject rObjString = new RedisStringObject(messageBuffer.groupId());

        return client.getScript().evalAsync(RScript.Mode.READ_WRITE,
            "if ((redis.call('lpush', KEYS[1], ARGV[1])) > 0) then " +
                "return (redis.call('sadd', KEYS[2], ARGV[2]));" +
                "end",
            RScript.ReturnType.INTEGER,
            Arrays.asList("{" + topicPartition.toString() + "}" + messageGroup.toString(), topicPartition.toString()), rObjMessage, rObjString)
            .thenApplyAsync(result -> {
                return new MessageMetadata(
                    message.messageId(),
                    message.topicId(),
                    message.partitionId(),
                    new Random().nextInt());
            })
            .exceptionally(exception -> {
                throw new VBrokerException("Unable to add message to redis : " + exception.getMessage());
            });

    }

    @Override
    public CompletionStage<Set<String>> getUniqueGroups() {

        RSetAsync<RedisObject> topicPartitionSet = client.getSet(topicPartition.toString());
        return topicPartitionSet.readAllAsync().thenApplyAsync(result -> {
            if (result != null || result.size() != 0) {
                return result
                    .stream()
                    .map(entry -> ((RedisStringObject) entry).getStringData())
                    .collect(Collectors.toSet());
            } else {
                return null;
            }
        }).exceptionally(exception -> {
                throw new VBrokerException("Unable to get Unique Groups :" + ((Exception) exception).getMessage());
            }
        );
    }

    @Override
    public PeekingIterator<Message> iteratorFrom(String groupId, int seqNoFrom) {
        log.info("getting peeking iterator");
        MessageGroup messageGroup = new MessageGroup(groupId, topicPartition);
        RList<RedisObject> rList = client.getList("{" + topicPartition.toString() + "}" + messageGroup.toString());
        return super.iteratorFrom(rList, seqNoFrom);
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset(String group) {
        log.info(String.format("Fetching offset for group {}", group));
        MessageGroup messageGroup = new MessageGroup(group, topicPartition);
        RListAsync<RedisObject> rList = client.getList("{" + topicPartition.toString() + "}" + messageGroup.toString());
        return rList.sizeAsync();
    }

    @Override
    public PeekingIterator<Message> iteratorFrom(int seqNoFrom) {
        throw new UnsupportedOperationException("You cannot have a global iterator for partition for a grouped topic-partition");
    }

    @Override
    public CompletionStage<Integer> getCurrentOffset() {
        throw new UnsupportedOperationException("Global offset is not defined for ungrouped topic-partition");
    }
}
