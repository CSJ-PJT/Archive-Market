package com.csj.archive.market.runtime;

import java.time.Instant;
import java.util.Map;

public record RuntimeEventResponse(
        String eventId,
        String idempotencyKey,
        String sourceService,
        String targetService,
        String sourceSystem,
        String targetSystem,
        String domain,
        String eventType,
        String entityType,
        String entityId,
        String orderId,
        String correlationId,
        String causationId,
        String simulationRunId,
        String settlementCycleId,
        String workdayId,
        String status,
        String severity,
        String displayLabel,
        Instant occurredAt,
        int hopCount,
        int maxHop,
        String cursor,
        Map<String, Object> metadata
) {
}
