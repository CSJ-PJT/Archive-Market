package com.csj.archive.market.runtime;

import java.time.Instant;

public record RuntimeStatusResponse(
        String service,
        boolean runtimeActive,
        boolean autoRunEnabled,
        String schedulerStatus,
        Instant lastWorkAt,
        Instant lastEventAt,
        int eventsProducedLastTick,
        int eventsConsumedLastTick,
        long backlogCount,
        long oldestBacklogAgeSeconds,
        String latestCursor,
        String degradedReason,
        String pipelineStatus
) {
}
