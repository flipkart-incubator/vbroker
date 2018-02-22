package com.flipkart.vbroker.utils;

import java.util.Random;

public class IdGenerator {

    private static Random random = new Random();

    public static short randomId() {
        return (short) random.nextInt(Short.MAX_VALUE + 1);
    }
}
