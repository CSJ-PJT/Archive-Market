package com.csj.archive.market.capital;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "market_workforce_allocation")
public class MarketWorkforceAllocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "workforce_role", nullable = false, unique = true)
    private WorkforceRole workforceRole;

    @Column(name = "headcount", nullable = false)
    private int headcount;

    @Column(name = "capacity_per_day", nullable = false)
    private int capacityPerDay;

    @Column(name = "wage_per_day", nullable = false, precision = 19, scale = 2)
    private BigDecimal wagePerDay;

    @Column(name = "productivity_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal productivityScore;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MarketWorkforceAllocationEntity() {
    }

    public MarketWorkforceAllocationEntity(WorkforceRole workforceRole, int headcount, int capacityPerDay,
                                           BigDecimal wagePerDay, BigDecimal productivityScore, boolean enabled) {
        this.workforceRole = workforceRole;
        this.headcount = headcount;
        this.capacityPerDay = capacityPerDay;
        this.wagePerDay = wagePerDay;
        this.productivityScore = productivityScore;
        this.enabled = enabled;
    }

    public void allocate(int headcount, int capacityPerDay, BigDecimal wagePerDay, BigDecimal productivityScore) {
        this.headcount = headcount;
        this.capacityPerDay = capacityPerDay;
        this.wagePerDay = wagePerDay;
        this.productivityScore = productivityScore;
        this.enabled = true;
    }

    public WorkforceRole getWorkforceRole() {
        return workforceRole;
    }

    public int getHeadcount() {
        return headcount;
    }

    public int getCapacityPerDay() {
        return capacityPerDay;
    }

    public BigDecimal getWagePerDay() {
        return wagePerDay;
    }

    public BigDecimal getProductivityScore() {
        return productivityScore;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
