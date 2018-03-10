package com.flipkart.vbroker.controller;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class ChildrenCacheTest {

    public static void main(String[] args) {

        List<String> children = Arrays.asList("1", "2", "3");
        ChildrenCache cache = new ChildrenCache(children);

        List<String> diff = cache.diffAndSet(Arrays.asList("1", "2", "3", "4", "5"));
        assertEquals(diff.size(), 2);
        assertEquals(diff.get(0), "4");
        assertEquals(diff.get(1), "5");
    }
}
