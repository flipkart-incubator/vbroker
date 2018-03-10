package com.flipkart.vbroker.subscribers;

import com.flipkart.vbroker.core.PartSubscription;
import com.flipkart.vbroker.flatbuf.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class UnGroupedIterableMessage implements IterableMessage {
    private static final Map<Message, AtomicBoolean> messageLockMap = new ConcurrentHashMap<>();
    @Getter
    private final Message message;
    private final PartSubscription partSubscription;
    private QType qType;
    private volatile AtomicBoolean lock;

    public UnGroupedIterableMessage(Message message,
                                    PartSubscription partSubscription,
                                    AtomicBoolean lock) {
        this.message = message;
        this.partSubscription = partSubscription;
        this.qType = QType.MAIN;
        this.lock = lock;
    }

    public static UnGroupedIterableMessage getInstance(Message message, PartSubscription partSubscription) {
        messageLockMap.computeIfAbsent(message, message1 -> new AtomicBoolean(false));
        return new UnGroupedIterableMessage(message, partSubscription, messageLockMap.get(message));
    }

    @Override
    public synchronized boolean isUnlocked() {
        return !lock.get();
    }

    @Override
    public synchronized boolean lock() {
        log.info("CurrLockState for msg {}: {}", message.messageId(), lock.get());
        return lock.compareAndSet(false, true);
    }

    @Override
    public synchronized void unlock() {
        log.info("Unlocked msg {}", message.messageId());
        lock.set(false);
    }

    @Override
    public String getGroupId() {
        return message.groupId();
    }

    @Override
    public PartSubscription getPartSubscription() {
        return partSubscription;
    }

    @Override
    public int subscriptionId() {
        return partSubscription.getSubscriptionId();
    }

    @Override
    public int getTopicId() {
        return partSubscription.getTopicPartition().getTopicId();
    }

    @Override
    public QType getQType() {
        return qType;
    }

    @Override
    public void setQType(QType qType) {
        this.qType = qType;
    }
}
