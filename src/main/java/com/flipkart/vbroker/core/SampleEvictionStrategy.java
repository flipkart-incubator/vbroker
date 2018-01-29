package com.flipkart.vbroker.core;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

@Slf4j
public class SampleEvictionStrategy implements EvictionStrategy {
    @Override
    public boolean shouldEvict(int capacity) {
        int sampleCapacityThreshold = 29; //should come from capacity planner actually
        if (capacity > sampleCapacityThreshold) return true;
        return false;
    }

    @Override
    public void evict(Object parent, Object member, L3Provider l3Provider) {
        Iterator it = ((CapacityManagedList) member).iterator();
        log.info("Evicting member with groupId {} from parent with {} no. of groupIds", ((CapacityManagedList) ((CapacityManagedList) member)).getGroupId(), ((CapacityManagedMap) parent).size());
        while (it.hasNext()) {
            l3Provider.write("sample-key", it.next());
        }

    }
}
