package com.csj.archive.market.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "market_outbox_event")
public class MarketOutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_service", nullable = false)
    private OutboxTargetService targetService;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MarketOutboxEntity() {
    }

    public MarketOutboxEntity(String eventId, String idempotencyKey, OutboxTargetService targetService,
                              String eventType, String aggregateType, String aggregateId, String payload) {
        this.eventId = eventId;
        this.idempotencyKey = idempotencyKey;
        this.targetService = targetService;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public OutboxTargetService getTargetService() {
        return targetService;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markDryRun(Instant now) {
        this.status = OutboxStatus.DRY_RUN;
        this.publishedAt = now;
        this.lastError = null;
    }

    public void markPublished(Instant now) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
        this.lastError = null;
    }

    public void markSkipped(Instant now, String reason) {
        this.status = OutboxStatus.SKIPPED;
        this.publishedAt = now;
        this.lastError = reason;
    }

    public void markFailed(String error, Instant nextRetryAt) {
        this.status = OutboxStatus.RETRY;
        this.retryCount++;
        this.lastError = error;
        this.nextRetryAt = nextRetryAt;
    }
}
