package com.csj.archive.market.capital;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "market_workday_snapshot")
public class MarketWorkdaySnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_id", nullable = false, unique = true)
    private String snapshotId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "order_count", nullable = false)
    private long orderCount;

    @Column(name = "processing_capacity", nullable = false)
    private long processingCapacity;

    @Column(name = "backlog_count", nullable = false)
    private long backlogCount;

    @Column(name = "available_cash", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableCash;

    @Column(name = "working_capital", nullable = false, precision = 19, scale = 2)
    private BigDecimal workingCapital;

    @Column(name = "payroll_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal payrollCost;

    @Column(name = "net_profit", nullable = false, precision = 19, scale = 2)
    private BigDecimal netProfit;

    @Column(name = "productivity_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal productivityScore;

    @Column(name = "recommendation", nullable = false)
    private String recommendation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MarketWorkdaySnapshotEntity() {
    }

    public MarketWorkdaySnapshotEntity(String snapshotId, LocalDate workDate, long orderCount, long processingCapacity,
                                       long backlogCount, BigDecimal availableCash, BigDecimal workingCapital,
                                       BigDecimal payrollCost, BigDecimal netProfit, BigDecimal productivityScore,
                                       String recommendation) {
        this.snapshotId = snapshotId;
        this.workDate = workDate;
        this.orderCount = orderCount;
        this.processingCapacity = processingCapacity;
        this.backlogCount = backlogCount;
        this.availableCash = availableCash;
        this.workingCapital = workingCapital;
        this.payrollCost = payrollCost;
        this.netProfit = netProfit;
        this.productivityScore = productivityScore;
        this.recommendation = recommendation;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public long getProcessingCapacity() {
        return processingCapacity;
    }

    public long getBacklogCount() {
        return backlogCount;
    }

    public BigDecimal getAvailableCash() {
        return availableCash;
    }

    public BigDecimal getWorkingCapital() {
        return workingCapital;
    }

    public BigDecimal getPayrollCost() {
        return payrollCost;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public BigDecimal getProductivityScore() {
        return productivityScore;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
