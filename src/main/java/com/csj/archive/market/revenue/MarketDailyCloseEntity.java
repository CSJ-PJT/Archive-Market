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
@Table(name = "market_daily_close")
public class MarketDailyCloseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "close_id", nullable = false, unique = true)
    private String closeId;

    @Column(name = "close_date", nullable = false)
    private LocalDate closeDate;

    @Column(name = "total_revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "total_profit", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalProfit;

    @Column(name = "order_count", nullable = false)
    private long orderCount;

    @Column(name = "return_count", nullable = false)
    private long returnCount;

    @Column(name = "claim_count", nullable = false)
    private long claimCount;

    @Column(name = "status", nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected MarketDailyCloseEntity() {
    }

    public MarketDailyCloseEntity(String closeId, LocalDate closeDate, BigDecimal totalRevenue, BigDecimal totalCost,
                                  BigDecimal totalProfit, long orderCount, long returnCount, long claimCount,
                                  String status, Instant completedAt) {
        this.closeId = closeId;
        this.closeDate = closeDate;
        this.totalRevenue = totalRevenue;
        this.totalCost = totalCost;
        this.totalProfit = totalProfit;
        this.orderCount = orderCount;
        this.returnCount = returnCount;
        this.claimCount = claimCount;
        this.status = status;
        this.completedAt = completedAt;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }
}
