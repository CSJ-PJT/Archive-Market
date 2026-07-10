package com.csj.archive.market.simulation;

import java.time.LocalDate;
import java.util.Map;

public record SimulationResult(
        String simulationRunId,
        LocalDate date,
        int requestedCount,
        int ordersCreated,
        int paymentsCaptured,
        int returnsCreated,
        int claimsCreated,
        Map<String, Object> summary
) {
}
