package com.flipkart.vbroker.ioengine;

import com.flipkart.vbroker.entities.Message;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class MessageService {

    private final Queue<Message> messageQueue = new ArrayBlockingQueue<>(100);

    public void add(Message message) {
        messageQueue.add(message);
    }

    public boolean hasNext() {
        return messageQueue.peek() != null;
    }

    public Message poll() {
        return messageQueue.poll();
    }

    public int size() {
        return messageQueue.size();
    }
}
