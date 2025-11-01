package com.sucheth.riskwatch.dto.api;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.model.enums.RiskLevel;

@Data
@Builder
public class TransactionResponse {
    
    private String transactionId;
    private String userId;
    private double riskScore;
    private RiskLevel riskLevel;
    private List<String> reasons;
    private Instant evaluatedAt;

    public static TransactionResponse from(Transaction tx) {
        return TransactionResponse.builder()
        .transactionId(tx.getTransactionId())
        .userId(tx.getUserId())
        .riskScore(tx.getRiskScore())
        .riskLevel(tx.getRiskLevel())
        .reasons(tx.getReasons())
        .evaluatedAt(Instant.now())
        .build();
    }
}