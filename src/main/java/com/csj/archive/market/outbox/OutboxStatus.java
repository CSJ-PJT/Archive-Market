package com.csj.archive.market.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    RETRY,
    FAILED,
    SKIPPED,
    DRY_RUN
}
