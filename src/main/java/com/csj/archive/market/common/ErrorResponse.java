package com.csj.archive.market.common;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        boolean success,
        String code,
        String message,
        String traceId,
        List<String> details,
        Instant timestamp
) {
    public static ErrorResponse of(String code, String message, String traceId, List<String> details) {
        return new ErrorResponse(false, code, message, traceId, details, Instant.now());
    }
}
