package com.riskwatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    private Integer totalTransactions;
    private Double averageRiskScore;
    private Integer highRiskTransactionCount;
    private Instant lastTransactionTime;

    private String userRiskLevel;
    private Boolean isFlagged;
}