package com.project.riskwatch.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.project.riskwatch.model.Transaction;
import com.project.riskwatch.dto.TransactionRequest;
import com.project.riskwatch.dto.TransactionResponse;
import com.project.riskwatch.repository.TransactionRepository;
import com.project.riskwatch.risk.RiskEvaluator;
import com.project.riskwatch.risk.RiskEvaluationResult;
import com.project.riskwatch.service.UserRiskProfileService;

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
        .timestamp(request.getTimeStamp())
        .deviceId(request.getDeviceId())
        .location(request.getLocation())
        .build();

        RiskEvaluationResult result = riskEvaluator.evaluate(tx);
        tx.setRiskScore(result.getScore());
        tx.setRiskLevel(result.getLevel());
        tx.setReasons(result.getReasons());

        transactionRepository.save(tx);

        UserRiskProfileService.updateUserProfile(tx);

        return TransactionResponse.from(tx);
    }
}