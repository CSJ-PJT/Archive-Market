package com.csj.archive.market.runtime;

import java.time.Instant;
import java.util.Map;

public record RuntimeEventResponse(
        String eventId,
        String sourceService,
        String domain,
        String eventType,
        String entityType,
        String entityId,
        String correlationId,
        String causationId,
        String status,
        String severity,
        String displayLabel,
        Instant occurredAt,
        Map<String, Object> metadata
) {
}
