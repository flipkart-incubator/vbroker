package com.flipkart.vbroker.subscribers;

import java.util.Iterator;

public class UngroupedPartSubscriber implements Iterable<IMessageWithGroup> {


    @Override
    public Iterator<IMessageWithGroup> iterator() {
        return null;
    }
}
