package com.sucheth.riskwatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRiskProfile {

    @Id
    private String userId;

    @Column(nullable = false)
    private Integer totalTransactions;

    @Column(nullable = false)
    private Double averageRiskScore;

    @Column(nullable = false)
    private Integer highRiskTransactionCount;

    @Column(nullable = false)
    private Instant lastTransactionTime;

    @Column(nullable = false)
    private String userRiskLevel;

    @Column(nullable = false)
    private Boolean isFlagged;

    public void incrementTransactionCount() {
        if (this.totalTransactions == null) {
            this.totalTransactions = 0;
        }
        this.totalTransactions++;
    }
}