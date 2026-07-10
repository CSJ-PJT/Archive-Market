package com.csj.archive.market.revenue;

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
@Table(name = "market_profit_snapshot")
public class MarketProfitSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_id", nullable = false, unique = true)
    private String snapshotId;

    @Column(name = "simulation_run_id")
    private String simulationRunId;

    @Column(name = "settlement_cycle_id")
    private String settlementCycleId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "revenue_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal revenueAmount;

    @Column(name = "cost_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "profit_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal profitAmount;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    @Column(name = "burn_rate", nullable = false, precision = 19, scale = 2)
    private BigDecimal burnRate;

    @Column(name = "bankruptcy_risk", nullable = false)
    private String bankruptcyRisk;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MarketProfitSnapshotEntity() {
    }

    public MarketProfitSnapshotEntity(String snapshotId, String simulationRunId, String settlementCycleId,
                                      LocalDate snapshotDate, BigDecimal revenueAmount, BigDecimal costAmount,
                                      BigDecimal profitAmount, BigDecimal cashBalance, BigDecimal burnRate,
                                      String bankruptcyRisk) {
        this.snapshotId = snapshotId;
        this.simulationRunId = simulationRunId;
        this.settlementCycleId = settlementCycleId;
        this.snapshotDate = snapshotDate;
        this.revenueAmount = revenueAmount;
        this.costAmount = costAmount;
        this.profitAmount = profitAmount;
        this.cashBalance = cashBalance;
        this.burnRate = burnRate;
        this.bankruptcyRisk = bankruptcyRisk;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public BigDecimal getRevenueAmount() {
        return revenueAmount;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public BigDecimal getProfitAmount() {
        return profitAmount;
    }

    public String getBankruptcyRisk() {
        return bankruptcyRisk;
    }
}
