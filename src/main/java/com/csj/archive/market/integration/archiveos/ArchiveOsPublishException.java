package com.csj.archive.market.integration.archiveos;

public class ArchiveOsPublishException extends RuntimeException {

    private final boolean retryable;

    public ArchiveOsPublishException(String reason, boolean retryable) {
        super(reason);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
