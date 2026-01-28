package com.sucheth.riskwatch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.time.Duration;
import com.sucheth.riskwatch.repository.TransactionRepository;
import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.dto.internal.RiskEvaluationResult;
import com.sucheth.riskwatch.model.enums.RiskLevel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RiskEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(RiskEvaluator.class);

    @Value("${risk.thresholds.amount}")
    private Double amountThreshold;

    @Value("${risk.thresholds.velocity.limit}")
    private int velocityLimit;

    @Value("${risk.thresholds.velocity.window-minutes}")
    private int velocityWindowMinutes;

    private final TransactionRepository transactionRepository;

    public RiskEvaluationResult evaluate(Transaction tx) {
        logger.debug("Evaluating risk for transaction: transactionId={}, userId={}, amount={}", 
                tx.getTransactionId(), tx.getUserId(), tx.getAmount());

        if (amountThreshold == null) {
            logger.error("Amount threshold not configured, using default value");
            throw new IllegalStateException("Risk threshold configuration is missing");
        }

        List<String> reasons = new ArrayList<>();
        double score = 0.0;

        if (tx.getAmount() != null && tx.getAmount() > amountThreshold) {
            score += 0.3;
            reasons.add("Transaction amount exceeds threshold.");
            logger.info("High amount transaction detected: transactionId={}, amount={}, threshold={}", 
                    tx.getTransactionId(), tx.getAmount(), amountThreshold);
        }

        if (tx.getTimestamp() == null) {
            throw new IllegalArgumentException("Transaction timestamp is required");
        }

        if (tx.getUserId() == null) {
            throw new IllegalArgumentException("Transaction userId is required");
        }
        
        Instant cutoff = tx.getTimestamp().minus(Duration.ofMinutes(velocityWindowMinutes));
        long recentCount = transactionRepository.findByUserIdAndTimestampAfter(tx.getUserId(), cutoff)
            .stream()
            .filter(t -> !t.getTransactionId().equals(tx.getTransactionId()))
            .count();
        
        if (recentCount > velocityLimit) {
            score += 0.4;
            reasons.add("High transaction velocity detected.");
            logger.info("High velocity transaction detected: transactionId={}, userId={}, recentCount={}, limit={}", 
                    tx.getTransactionId(), tx.getUserId(), recentCount, velocityLimit);
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

        logger.info("Risk evaluation result: transactionId={}, riskScore={}, riskLevel={}, reasonsCount={}", 
                tx.getTransactionId(), score, level, reasons.size());

        return new RiskEvaluationResult(score, level, reasons);
    }
}