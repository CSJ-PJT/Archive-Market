package com.csj.archive.market.simulation;

import java.time.LocalDate;

public record SimulationRequest(
        int count,
        Long seed,
        LocalDate date
) {
}
