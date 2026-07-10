package com.csj.archive.market.customer;

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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "market_customer")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private SyntheticCustomer customerType;

    @Column(name = "risk_level", nullable = false)
    private int riskLevel;

    @Column(name = "synthetic_name", nullable = false)
    private String syntheticName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerEntity() {
    }

    public CustomerEntity(String customerId, SyntheticCustomer customerType, int riskLevel, String syntheticName) {
        this.customerId = customerId;
        this.customerType = customerType;
        this.riskLevel = riskLevel;
        this.syntheticName = syntheticName;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public SyntheticCustomer getCustomerType() {
        return customerType;
    }

    public int getRiskLevel() {
        return riskLevel;
    }

    public String getSyntheticName() {
        return syntheticName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
