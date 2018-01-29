package com.flipkart.vbroker.core;

public interface EvictionStrategy {
    public boolean shouldEvict(int capacity);

    public void evict(Object parent, Object member, L3Provider l3Provider);
}
