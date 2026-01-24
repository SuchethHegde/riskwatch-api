package com.sucheth.riskwatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import com.sucheth.riskwatch.model.enums.UserRiskLevel;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRiskLevel userRiskLevel;

    @Column(nullable = false)
    private Boolean isFlagged;
}