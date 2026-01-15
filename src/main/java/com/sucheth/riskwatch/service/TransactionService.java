package com.sucheth.riskwatch.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.dto.api.TransactionRequest;
import com.sucheth.riskwatch.dto.api.TransactionResponse;
import com.sucheth.riskwatch.repository.TransactionRepository;
import com.sucheth.riskwatch.dto.internal.RiskEvaluationResult;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RiskEvaluator riskEvaluator;
    private final UserRiskProfileService userRiskProfileService;

    @Transactional
    public TransactionResponse evaluateAndSave(TransactionRequest request) {
        Transaction tx = Transaction.builder().transactionId(request.getTransactionId())
        .userId(request.getUserId())
        .amount(request.getAmount())
        .timestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now())
        .deviceId(request.getDeviceId())
        .location(request.getLocation())
        .build();

        RiskEvaluationResult result = riskEvaluator.evaluate(tx);
        tx.setRiskScore(result.getScore());
        tx.setRiskLevel(result.getLevel());
        tx.setReasons(result.getReasons());

        transactionRepository.save(tx);

        userRiskProfileService.updateUserRiskProfile(tx);

        return TransactionResponse.from(tx);
    }

    public List<TransactionResponse> getUserTransactions(String userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId)
            .stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .toList();
        
        return transactions.stream()
            .map(TransactionResponse::from)
            .toList();
    }
}