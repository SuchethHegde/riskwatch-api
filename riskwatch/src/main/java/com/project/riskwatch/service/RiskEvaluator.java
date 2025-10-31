package com.project.riskwatch.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.time.Duration;
import com.project.riskwatch.repository.TransactionRepository;
import com.project.riskwatch.model.Transaction;
import com.project.riskwatch.model.RiskEvaluationResult;
import com.project.riskwatch.model.RiskLevel;

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
        int score = 0;

        if (tx.getAmount() > amountThreshold) {
            score += 60;
            reasons.add("Transaction amount exceeds threshold.");
        }

        Instant cutoff = tx.getTimeStamp().minus(Duration.ofMinutes(velocityWindowMinutes));
        long recentCount = transactionRepository.findByUserId(tx.getUserId()).stream().filter(t -> t.getTimeStamp().isAfter(cutoff)).count();
        
        if (recentCount > velocityLimit) {
            score += 40;
            reasons.add("High transaction velocity detected.");
        }

        RiskLevel level = (score > 70)? RiskLevel.HIGH: (score > 30)? RiskLevel.MEDIUM: RiskLevel.LOW;

        return new RiskEvaluationResult(score, level, reasons);
    }
}