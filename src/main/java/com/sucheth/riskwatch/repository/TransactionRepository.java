package com.sucheth.riskwatch.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.model.enums.RiskLevel;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(String userId);
    List<Transaction> findByRiskLevel(RiskLevel riskLevel);
}