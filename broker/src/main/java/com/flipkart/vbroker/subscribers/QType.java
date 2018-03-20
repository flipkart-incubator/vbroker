package com.flipkart.vbroker.subscribers;

public enum QType {
    MAIN(0), SIDELINE(1), RETRY_1(2), RETRY_2(3), RETRY_3(4);

    private final int qTypeIdx;

    QType(int qTypeIdx) {
        this.qTypeIdx = qTypeIdx;
    }

    public static QType getQType(int qTypeIdx) {
        return QType.values()[qTypeIdx];
    }

    public static QType retryQType(int retryNo) {
        return QType.valueOf("RETRY_" + retryNo);
    }

    public int getqTypeIdx() {
        return qTypeIdx;
    }
}
