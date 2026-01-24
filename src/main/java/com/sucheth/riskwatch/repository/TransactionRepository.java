package com.sucheth.riskwatch.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.model.enums.RiskLevel;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(String userId);

    List<Transaction> findByRiskLevel(RiskLevel riskLevel);

    List<Transaction> findByUserIdAndTimestampAfter(String userId, Instant cutoff);

    java.util.Optional<Transaction> findByTransactionId(String transactionId);

    @Query("SELECT t FROM Transaction t WHERE t.riskScore > :minScore")
    List<Transaction> findTransactionsAboveRiskScore(@Param("minScore") double minScore);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.timestamp DESC")
    List<Transaction> findRecentTransactions(
            @Param("userId") String userId,
            org.springframework.data.domain.Pageable pageable
    );

    org.springframework.data.domain.Page<Transaction> findByUserIdOrderByTimestampDesc(String userId, org.springframework.data.domain.Pageable pageable);
}