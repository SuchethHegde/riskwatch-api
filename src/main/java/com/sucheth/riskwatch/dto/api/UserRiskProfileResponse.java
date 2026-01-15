package com.sucheth.riskwatch.dto.api;

import com.sucheth.riskwatch.model.UserRiskProfile;
import com.sucheth.riskwatch.model.enums.UserRiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserRiskProfileResponse {
    private String userId;
    private Integer totalTransactions;
    private Double averageRiskScore;
    private Integer highRiskTransactionCount;
    private Instant lastTransactionTime;
    private UserRiskLevel userRiskLevel;
    private Boolean isFlagged;

    public static UserRiskProfileResponse from(UserRiskProfile profile) {
        return UserRiskProfileResponse.builder()
                .userId(profile.getUserId())
                .totalTransactions(profile.getTotalTransactions())
                .averageRiskScore(profile.getAverageRiskScore())
                .highRiskTransactionCount(profile.getHighRiskTransactionCount())
                .lastTransactionTime(profile.getLastTransactionTime())
                .userRiskLevel(profile.getUserRiskLevel())
                .isFlagged(profile.getIsFlagged())
                .build();
    }
}