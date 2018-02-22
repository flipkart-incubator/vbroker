package com.flipkart.vbroker.subscribers;

public enum QType {
    MAIN, SIDELINE, RETRY_1, RETRY_2, RETRY_3;

    public static QType retryQType(int retryNo) {
        return QType.valueOf("RETRY_" + retryNo);
    }
}
