package com.csj.archive.market.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "market_event_inbox")
public class MarketInboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "source_service", nullable = false)
    private String sourceService;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MarketInboxStatus status;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    protected MarketInboxEntity() {
    }

    public MarketInboxEntity(String eventId, String idempotencyKey, String sourceService, String eventType,
                             String payload, MarketInboxStatus status, Instant receivedAt, Instant processedAt,
                             String failureReason) {
        this.eventId = eventId;
        this.idempotencyKey = idempotencyKey;
        this.sourceService = sourceService;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.receivedAt = receivedAt;
        this.processedAt = processedAt;
        this.failureReason = failureReason;
    }

    public String getEventId() {
        return eventId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getEventType() {
        return eventType;
    }

    public MarketInboxStatus getStatus() {
        return status;
    }
}
