package com.csj.archive.market.capital;

import java.math.BigDecimal;
import java.util.Map;

public record WorkforceAllocationRequest(
        Map<WorkforceRole, RoleAllocation> allocations
) {
    public record RoleAllocation(
            Integer headcount,
            Integer capacityPerDay,
            BigDecimal wagePerDay,
            BigDecimal productivityScore
    ) {
    }
}
