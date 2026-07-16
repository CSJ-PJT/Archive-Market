package com.csj.archive.market.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHING,
    PUBLISHED,
    RETRY,
    RETRY_WAIT,
    FAILED,
    SKIPPED,
    DEAD_LETTER,
    DRY_RUN
}
