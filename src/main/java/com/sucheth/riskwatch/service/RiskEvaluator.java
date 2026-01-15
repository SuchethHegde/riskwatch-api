package com.sucheth.riskwatch.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.time.Duration;
import com.sucheth.riskwatch.repository.TransactionRepository;
import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.dto.internal.RiskEvaluationResult;
import com.sucheth.riskwatch.model.enums.RiskLevel;

@Component
public class RiskEvaluator {

    @Value("${risk.thresholds.amount}")
    private Double amountThreshold;

    @Value("${risk.thresholds.velocity.limit}")
    private int velocityLimit;

    @Value("${risk.thresholds.velocity.window-minutes}")
    private int velocityWindowMinutes;

    @Autowired
    private TransactionRepository transactionRepository;

    public RiskEvaluationResult evaluate(Transaction tx) {
        List<String> reasons = new ArrayList<>();
        double score = 0.0;

        if (tx.getAmount() > amountThreshold) {
            score += 0.3;
            reasons.add("Transaction amount exceeds threshold.");
        }

        Instant cutoff = tx.getTimestamp().minus(Duration.ofMinutes(velocityWindowMinutes));
        long recentCount = transactionRepository.findByUserId(tx.getUserId()).stream()
            .filter(t -> t.getTimestamp().isAfter(cutoff) && !t.getTransactionId().equals(tx.getTransactionId()))
            .count();
        
        if (recentCount > velocityLimit) {
            score += 0.4;
            reasons.add("High transaction velocity detected.");
        }

        score = Math.min(1.0, score);

        RiskLevel level;
        if (score >= 0.7) {
            level = RiskLevel.HIGH;
        } else if (score >= 0.4) {
            level = RiskLevel.MEDIUM;
        } else {
            level = RiskLevel.LOW;
        }

        return new RiskEvaluationResult(score, level, reasons);
    }
}