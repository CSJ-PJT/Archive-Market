package com.csj.archive.market.order;

import com.csj.archive.market.customer.SyntheticCustomer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "market_order")
public class MarketOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private SyntheticCustomer customerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "total_order_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalOrderAmount;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval;

    @Column(name = "root_correlation_id", unique = true)
    private String rootCorrelationId;

    @Column(name = "last_event_id")
    private String lastEventId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MarketOrderItemEntity> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MarketOrderEntity() {
    }

    public MarketOrderEntity(String orderId, String customerId, SyntheticCustomer customerType,
                             BigDecimal totalOrderAmount, BigDecimal discountAmount, BigDecimal paymentAmount,
                             String currency, int riskScore, boolean requiresApproval, String rootCorrelationId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerType = customerType;
        this.orderStatus = OrderStatus.CREATED;
        this.totalOrderAmount = totalOrderAmount;
        this.discountAmount = discountAmount;
        this.paymentAmount = paymentAmount;
        this.currency = currency;
        this.riskScore = riskScore;
        this.requiresApproval = requiresApproval;
        this.rootCorrelationId = rootCorrelationId;
    }

    public void addItem(MarketOrderItemEntity item) {
        items.add(item);
        item.assignOrder(this);
    }

    public void changeStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void advanceCausation(String eventId) {
        this.lastEventId = eventId;
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public SyntheticCustomer getCustomerType() {
        return customerType;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public BigDecimal getTotalOrderAmount() {
        return totalOrderAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public String getRootCorrelationId() {
        return rootCorrelationId;
    }

    public String getLastEventId() {
        return lastEventId;
    }

    public List<MarketOrderItemEntity> getItems() {
        return items;
    }
}
