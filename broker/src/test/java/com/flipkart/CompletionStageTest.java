package com.flipkart;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by kaushal.hooda on 21/02/18.
 */
public class CompletionStageTest {

    @Test
    public void shouldComposeMultipleFuturesInForLoop() {
        CompletableFuture<List<Integer>> cf = CompletableFuture.completedFuture(new ArrayList<>());
        for (int i = 0; i < 10; i++) {
            cf = cf.thenCompose(integers -> {
                integers.add(integers.size());
                return CompletableFuture.completedFuture(integers);
            });
        }
        List<Integer> ints = cf.join();
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        Assert.assertEquals(expected, ints);
    }
}
