package com.flipkart.vbroker.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public enum SingletonTemp {
    INSTANCE;

    public void print() {
        System.out.println("Hello obj: " + this.hashCode() + "; by thread: " + Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newWorkStealingPool();

        IntStream.range(0, 10)
            .forEach(i -> {
                executorService.submit(SingletonTemp.INSTANCE::print);
            });
    }
}

