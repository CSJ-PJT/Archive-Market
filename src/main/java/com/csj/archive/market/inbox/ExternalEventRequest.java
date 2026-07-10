package com.csj.archive.market.inbox;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ExternalEventRequest(
        @NotBlank String eventId,
        @NotBlank String idempotencyKey,
        @NotBlank String source,
        @NotBlank String eventType,
        @NotNull Integer schemaVersion,
        @NotBlank String occurredAt,
        String simulationRunId,
        String settlementCycleId,
        @NotBlank String correlationId,
        @NotBlank String causationId,
        @NotNull Integer hopCount,
        @NotNull Integer maxHop,
        @NotNull Map<String, Object> payload
) {
}
