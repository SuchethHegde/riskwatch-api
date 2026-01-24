package com.sucheth.riskwatch.service;

import com.sucheth.riskwatch.dto.internal.RiskEvaluationResult;
import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.model.enums.RiskLevel;
import com.sucheth.riskwatch.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskEvaluatorTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RiskEvaluator riskEvaluator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(riskEvaluator, "amountThreshold", 100000.0);
        ReflectionTestUtils.setField(riskEvaluator, "velocityLimit", 3);
        ReflectionTestUtils.setField(riskEvaluator, "velocityWindowMinutes", 2);
    }

    @Test
    void evaluate_LowRiskTransaction_ReturnsLowRisk() {
        // Given
        Transaction tx = Transaction.builder()
                .transactionId("tx-001")
                .userId("user-001")
                .amount(50000.0)
                .timestamp(Instant.now())
                .build();

        when(transactionRepository.findByUserId("user-001")).thenReturn(Collections.emptyList());

        // When
        RiskEvaluationResult result = riskEvaluator.evaluate(tx);

        // Then
        assertNotNull(result);
        assertEquals(RiskLevel.LOW, result.getLevel());
        assertTrue(result.getScore() < 0.4);
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void evaluate_HighAmountTransaction_ReturnsMediumRisk() {
        // Given
        Transaction tx = Transaction.builder()
                .transactionId("tx-002")
                .userId("user-002")
                .amount(150000.0) // Exceeds threshold
                .timestamp(Instant.now())
                .build();

        when(transactionRepository.findByUserId("user-002")).thenReturn(Collections.emptyList());

        // When
        RiskEvaluationResult result = riskEvaluator.evaluate(tx);

        // Then
        assertNotNull(result);
        assertEquals(RiskLevel.MEDIUM, result.getLevel());
        assertTrue(result.getScore() >= 0.3);
        assertTrue(result.getReasons().contains("Transaction amount exceeds threshold."));
    }

    @Test
    void evaluate_HighVelocityTransaction_ReturnsMediumRisk() {
        // Given
        Instant now = Instant.now();
        Transaction tx = Transaction.builder()
                .transactionId("tx-003")
                .userId("user-003")
                .amount(50000.0)
                .timestamp(now)
                .build();

        // Create 4 recent transactions (exceeds limit of 3)
        List<Transaction> recentTransactions = List.of(
                createTransaction("tx-old-1", "user-003", now.minusSeconds(30)),
                createTransaction("tx-old-2", "user-003", now.minusSeconds(60)),
                createTransaction("tx-old-3", "user-003", now.minusSeconds(90)),
                createTransaction("tx-old-4", "user-003", now.minusSeconds(100))
        );

        when(transactionRepository.findByUserId("user-003")).thenReturn(recentTransactions);

        // When
        RiskEvaluationResult result = riskEvaluator.evaluate(tx);

        // Then
        assertNotNull(result);
        assertEquals(RiskLevel.MEDIUM, result.getLevel());
        assertTrue(result.getScore() >= 0.4);
        assertTrue(result.getReasons().contains("High transaction velocity detected."));
    }

    @Test
    void evaluate_HighAmountAndVelocity_ReturnsHighRisk() {
        // Given
        Instant now = Instant.now();
        Transaction tx = Transaction.builder()
                .transactionId("tx-004")
                .userId("user-004")
                .amount(150000.0) // Exceeds threshold
                .timestamp(now)
                .build();

        // Create 4 recent transactions
        List<Transaction> recentTransactions = List.of(
                createTransaction("tx-old-1", "user-004", now.minusSeconds(30)),
                createTransaction("tx-old-2", "user-004", now.minusSeconds(60)),
                createTransaction("tx-old-3", "user-004", now.minusSeconds(90)),
                createTransaction("tx-old-4", "user-004", now.minusSeconds(100))
        );

        when(transactionRepository.findByUserId("user-004")).thenReturn(recentTransactions);

        // When
        RiskEvaluationResult result = riskEvaluator.evaluate(tx);

        // Then
        assertNotNull(result);
        assertEquals(RiskLevel.HIGH, result.getLevel());
        assertTrue(result.getScore() >= 0.7);
        assertTrue(result.getReasons().contains("Transaction amount exceeds threshold."));
        assertTrue(result.getReasons().contains("High transaction velocity detected."));
    }

    @Test
    void evaluate_ScoreCappedAtOne() {
        // Given
        Instant now = Instant.now();
        Transaction tx = Transaction.builder()
                .transactionId("tx-005")
                .userId("user-005")
                .amount(200000.0) // Very high amount
                .timestamp(now)
                .build();

        // Create many recent transactions
        List<Transaction> recentTransactions = List.of(
                createTransaction("tx-old-1", "user-005", now.minusSeconds(10)),
                createTransaction("tx-old-2", "user-005", now.minusSeconds(20)),
                createTransaction("tx-old-3", "user-005", now.minusSeconds(30)),
                createTransaction("tx-old-4", "user-005", now.minusSeconds(40)),
                createTransaction("tx-old-5", "user-005", now.minusSeconds(50))
        );

        when(transactionRepository.findByUserId("user-005")).thenReturn(recentTransactions);

        // When
        RiskEvaluationResult result = riskEvaluator.evaluate(tx);

        // Then
        assertNotNull(result);
        assertTrue(result.getScore() <= 1.0);
    }

    @Test
    void evaluate_OldTransactionsNotCountedInVelocity() {
        // Given
        Instant now = Instant.now();
        Transaction tx = Transaction.builder()
                .transactionId("tx-006")
                .userId("user-006")
                .amount(50000.0)
                .timestamp(now)
                .build();

        // Create transactions older than the window (2 minutes = 120 seconds)
        List<Transaction> oldTransactions = List.of(
                createTransaction("tx-old-1", "user-006", now.minusSeconds(130)),
                createTransaction("tx-old-2", "user-006", now.minusSeconds(140)),
                createTransaction("tx-old-3", "user-006", now.minusSeconds(150))
        );

        when(transactionRepository.findByUserId("user-006")).thenReturn(oldTransactions);

        // When
        RiskEvaluationResult result = riskEvaluator.evaluate(tx);

        // Then
        assertNotNull(result);
        assertEquals(RiskLevel.LOW, result.getLevel());
        assertFalse(result.getReasons().contains("High transaction velocity detected."));
    }

    private Transaction createTransaction(String transactionId, String userId, Instant timestamp) {
        return Transaction.builder()
                .transactionId(transactionId)
                .userId(userId)
                .amount(1000.0)
                .timestamp(timestamp)
                .riskScore(0.1)
                .riskLevel(RiskLevel.LOW)
                .build();
    }
}
