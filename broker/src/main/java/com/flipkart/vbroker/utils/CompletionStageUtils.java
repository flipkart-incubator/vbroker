package com.flipkart.vbroker.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by kaushal.hooda on 22/02/18.
 */
public class CompletionStageUtils {
    /**
     * Given a list of CompletionStage<T>, generates a new CompletionStage that is completed
     * when all of the provided stages are completed, and returns a List<T> as it's result.
     */
    public static <T> CompletionStage<List<T>> listOfStagesToStageOfList(List<CompletionStage<T>> stages) {
        List<CompletableFuture<T>> futures = stages.stream()
            .map(CompletionStage::toCompletableFuture)
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
            .thenApply(v -> futures.stream()
                .map(CompletionStage::toCompletableFuture)
                .map(CompletableFuture::join)
                .collect(toList())
            );
    }
}
