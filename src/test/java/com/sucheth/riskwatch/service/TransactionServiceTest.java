package com.sucheth.riskwatch.service;

import com.sucheth.riskwatch.dto.api.TransactionRequest;
import com.sucheth.riskwatch.dto.api.TransactionResponse;
import com.sucheth.riskwatch.dto.internal.RiskEvaluationResult;
import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.model.enums.RiskLevel;
import com.sucheth.riskwatch.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RiskEvaluator riskEvaluator;

    @Mock
    private UserRiskProfileService userRiskProfileService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void evaluateAndSave_ValidRequest_ReturnsTransactionResponse() {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setTransactionId("tx-001");
        request.setUserId("user-001");
        request.setAmount(50000.0);
        request.setTimestamp(Instant.now());

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .transactionId("tx-001")
                .userId("user-001")
                .amount(50000.0)
                .timestamp(request.getTimestamp())
                .riskScore(0.2)
                .riskLevel(RiskLevel.LOW)
                .reasons(Collections.emptyList())
                .build();

        RiskEvaluationResult evaluationResult = new RiskEvaluationResult(0.2, RiskLevel.LOW, Collections.emptyList());

        when(riskEvaluator.evaluate(any(Transaction.class))).thenReturn(evaluationResult);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // When
        TransactionResponse response = transactionService.evaluateAndSave(request);

        // Then
        assertNotNull(response);
        assertEquals("tx-001", response.getTransactionId());
        assertEquals("user-001", response.getUserId());
        assertEquals(0.2, response.getRiskScore());
        assertEquals(RiskLevel.LOW, response.getRiskLevel());

        verify(riskEvaluator).evaluate(any(Transaction.class));
        verify(transactionRepository).save(any(Transaction.class));
        verify(userRiskProfileService).updateUserRiskProfile(any(Transaction.class));
    }

    @Test
    void evaluateAndSave_NullTimestamp_UsesCurrentTime() {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setTransactionId("tx-002");
        request.setUserId("user-002");
        request.setAmount(50000.0);
        request.setTimestamp(null);

        RiskEvaluationResult evaluationResult = new RiskEvaluationResult(0.2, RiskLevel.LOW, Collections.emptyList());
        Transaction savedTransaction = Transaction.builder()
                .id(2L)
                .transactionId("tx-002")
                .userId("user-002")
                .amount(50000.0)
                .riskScore(0.2)
                .riskLevel(RiskLevel.LOW)
                .reasons(Collections.emptyList())
                .build();

        when(riskEvaluator.evaluate(any(Transaction.class))).thenReturn(evaluationResult);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // When
        transactionService.evaluateAndSave(request);

        // Then
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(riskEvaluator).evaluate(transactionCaptor.capture());
        assertNotNull(transactionCaptor.getValue().getTimestamp());
    }

    @Test
    void getUserTransactions_UserExists_ReturnsSortedTransactions() {
        // Given
        String userId = "user-001";
        Instant now = Instant.now();

        List<Transaction> transactions = List.of(
                createTransaction(1L, "tx-001", userId, now.minusSeconds(100)),
                createTransaction(2L, "tx-002", userId, now),
                createTransaction(3L, "tx-003", userId, now.minusSeconds(50))
        );

        when(transactionRepository.findByUserId(userId)).thenReturn(transactions);

        // When
        List<TransactionResponse> responses = transactionService.getUserTransactions(userId);

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size());
        // Should be sorted by timestamp descending (newest first)
        assertEquals("tx-002", responses.get(0).getTransactionId());
        assertEquals("tx-003", responses.get(1).getTransactionId());
        assertEquals("tx-001", responses.get(2).getTransactionId());
    }

    @Test
    void getUserTransactions_NoTransactions_ReturnsEmptyList() {
        // Given
        String userId = "user-999";
        when(transactionRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        List<TransactionResponse> responses = transactionService.getUserTransactions(userId);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    private Transaction createTransaction(Long id, String transactionId, String userId, Instant timestamp) {
        return Transaction.builder()
                .id(id)
                .transactionId(transactionId)
                .userId(userId)
                .amount(1000.0)
                .timestamp(timestamp)
                .riskScore(0.1)
                .riskLevel(RiskLevel.LOW)
                .reasons(Collections.emptyList())
                .build();
    }
}
