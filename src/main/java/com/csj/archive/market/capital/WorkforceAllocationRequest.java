package com.csj.archive.market.capital;

import java.math.BigDecimal;
import java.util.Map;

public record WorkforceAllocationRequest(
        String workdayId,
        Map<WorkforceRole, RoleAllocation> allocations
) {
    public WorkforceAllocationRequest(Map<WorkforceRole, RoleAllocation> allocations) {
        this(null, allocations);
    }

    public record RoleAllocation(
            Integer headcount,
            Integer capacityPerDay,
            BigDecimal wagePerDay,
            BigDecimal productivityScore
    ) {
    }
}
