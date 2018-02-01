package com.flipkart.vbroker.core;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class VMap<K, V> implements Map, Observer {

    private final ConcurrentHashMap<K, V> kvConcurrentHashMap = new ConcurrentHashMap<>();
    private final EvictionStrategy evictionStrategy;
    private final L3Provider l3Provider;
    private int mapUsedCapacity = 0;

    public VMap(EvictionStrategy evictionStrategy, L3Provider l3Provider) {
        this.evictionStrategy = evictionStrategy;
        this.l3Provider = l3Provider;
    }

    @Override
    public int size() {
        return kvConcurrentHashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return kvConcurrentHashMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return kvConcurrentHashMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return kvConcurrentHashMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return kvConcurrentHashMap.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return kvConcurrentHashMap.put((K) key, (V) value);
    }

    @Override
    public Object remove(Object key) {
        return kvConcurrentHashMap.remove(key);
    }

    @Override
    public void putAll(Map m) {
        kvConcurrentHashMap.putAll(m);
    }

    @Override
    public void clear() {
        kvConcurrentHashMap.clear();
    }

    @Override
    public Set keySet() {
        return kvConcurrentHashMap.keySet();
    }

    @Override
    public Collection values() {
        return kvConcurrentHashMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return kvConcurrentHashMap.entrySet();
    }

    @Override
    public void update(Observable o, Object arg) {
        mapUsedCapacity += (int) arg;
        log.info("mapUsedCapacity changed to {}", mapUsedCapacity);
        if (evictionStrategy.shouldEvict(this.mapUsedCapacity)) {
            evictionStrategy.evict(this, o, l3Provider);
            ((VList) o).setLevel(VList.Level.L3);
        }
    }
}
