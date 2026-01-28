package com.sucheth.riskwatch.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final RiskEvaluator riskEvaluator;
    private final UserRiskProfileService userRiskProfileService;

    @Transactional
    public TransactionResponse evaluateAndSave(TransactionRequest request) {
        logger.info("Evaluating transaction: transactionId={}, userId={}, amount={}", 
                request.getTransactionId(), request.getUserId(), request.getAmount());

        // Check for duplicate transaction ID
        if (transactionRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            logger.warn("Duplicate transaction ID detected: transactionId={}", request.getTransactionId());
            throw new com.sucheth.riskwatch.exception.DuplicateTransactionException(request.getTransactionId());
        }

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

        logger.debug("Risk evaluation completed: transactionId={}, riskScore={}, riskLevel={}, reasons={}", 
                request.getTransactionId(), result.getScore(), result.getLevel(), result.getReasons());

        transactionRepository.save(tx);
        logger.debug("Transaction saved: transactionId={}, id={}", request.getTransactionId(), tx.getId());

        userRiskProfileService.updateUserRiskProfile(tx);
        logger.info("Transaction evaluation completed: transactionId={}, riskLevel={}", 
                request.getTransactionId(), result.getLevel());

        return TransactionResponse.from(tx);
    }

    public List<TransactionResponse> getUserTransactions(String userId) {
        logger.info("Fetching transactions for user: userId={}", userId);
        
        List<Transaction> transactions = transactionRepository.findByUserId(userId)
            .stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .toList();
        
        logger.debug("Found {} transactions for user: userId={}", transactions.size(), userId);
        
        return transactions.stream()
            .map(TransactionResponse::from)
            .toList();
    }

    public Page<TransactionResponse> getUserTransactions(String userId, int page, int size) {
        logger.info("Fetching transactions for user with pagination: userId={}, page={}, size={}", userId, page, size);
        
        // Validate pagination parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Transaction> transactionsPage = transactionRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        
        logger.debug("Found {} transactions for user: userId={} (page {} of {})", 
                transactionsPage.getNumberOfElements(), userId, page, transactionsPage.getTotalPages());
        
        return transactionsPage.map(TransactionResponse::from);
    }
}