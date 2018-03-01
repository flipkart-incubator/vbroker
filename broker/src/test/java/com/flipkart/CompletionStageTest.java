package com.flipkart;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by kaushal.hooda on 21/02/18.
 */

/**
 * Dummy samples to play with CompletionStages and futures
 */
@Slf4j
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


    @Test
    public void whatIfLocalVariableUpdated() {
        String s = "hello";
        //Need this otherwise we'll get error as we update s later
        String finalS = s;
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return finalS;
        });
        s = "goodbye";
        System.out.println(cf.join());
    }

    @Test
    public void allOfFuturesThrowsExceptionWhenOneThrows() {
        CompletableFuture<String> cf1 = CompletableFuture.completedFuture("a");
        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
            throw new NullPointerException();
        });

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(cf1, cf2);
        if (allFuture.isCompletedExceptionally()) {
            allFuture.exceptionally(throwable -> {
                log.error("AllFuture completed exceptionally with error {}", throwable);
                return null;
            });
        } else {
            log.info("Completed successfully");
        }
    }

    @Test
    public void shouldCatchRegularException() {
        LengthGetter lengthGetter = new LengthGetter();
        LengthMessageGetter lengthMessageGetter = new LengthMessageGetter(lengthGetter);
        String ret = lengthMessageGetter.getLengthMessage("exceptional").toCompletableFuture().join();
        Assert.assertEquals(ret, "Calculation failed due to: java.lang.RuntimeException: Too exceptional to handle");
    }

    @Test
    public void shouldCatchMockReturningExceptionalFuture() {
        LengthGetter lengthGetter = Mockito.mock(LengthGetter.class);
        Mockito.when(lengthGetter.getLength("exceptional"))
            .thenReturn(CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("whoops");
            }));
        LengthMessageGetter lengthMessageGetter = new LengthMessageGetter(lengthGetter);
        String ret = lengthMessageGetter.getLengthMessage("exceptional").toCompletableFuture().join();
        Assert.assertEquals(ret, "Calculation failed due to: java.lang.RuntimeException: whoops");
    }

    @Test(expectedExceptions = RuntimeException.class)
    void shouldNotCatchMockThrowingException() {
        LengthGetter lengthGetter = Mockito.mock(LengthGetter.class);
        Mockito.when(lengthGetter.getLength("exceptional"))
            .thenThrow(new RuntimeException("whoops"));
        LengthMessageGetter lengthMessageGetter = new LengthMessageGetter(lengthGetter);
        lengthMessageGetter.getLengthMessage("exceptional").toCompletableFuture().join();
    }


    @AllArgsConstructor
    public class LengthMessageGetter {
        private LengthGetter lengthGetter;

        public CompletionStage<String> getLengthMessage(String s) {
            return lengthGetter.getLength(s)
                .thenApply(len -> "Len was: ".concat(len.toString()))
                .exceptionally(throwable -> {
                    log.info("Catching an exception");
                    return "Calculation failed due to: ".concat(throwable.getMessage());
                })
                ;
        }
    }

    public class LengthGetter {
        public CompletableFuture<Integer> getLength(final String s) {
            log.info("getlength");
            return CompletableFuture.supplyAsync(() -> {
                if (s.equals("exceptional")) {
                    throw new RuntimeException("Too exceptional to handle");
                } else {
                    return s.length();
                }
            });
        }
    }


}
