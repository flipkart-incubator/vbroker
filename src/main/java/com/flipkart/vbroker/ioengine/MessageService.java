package com.flipkart.vbroker.ioengine;

import com.flipkart.vbroker.entities.Message;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MessageService {

    private final List<Message> messages = new LinkedList<>();

    public void store(Message message) {
        messages.add(message);
    }

    public Iterator<Message> messageIterator() {
        return messages.iterator();
    }

    public int size() {
        return messages.size();
    }
}
