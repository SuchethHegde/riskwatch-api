package com.sucheth.riskwatch.service;

import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.model.UserRiskProfile;
import com.sucheth.riskwatch.model.enums.RiskLevel;
import com.sucheth.riskwatch.model.enums.UserRiskLevel;
import com.sucheth.riskwatch.repository.UserRiskProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRiskProfileService {

    private final UserRiskProfileRepository userRiskProfileRepository;

    @Transactional
    public void updateUserRiskProfile(Transaction tx) {
        UserRiskProfile profile = userRiskProfileRepository.findById(tx.getUserId())
                .orElse(UserRiskProfile.builder()
                        .userId(tx.getUserId())
                        .totalTransactions(0)
                        .averageRiskScore(0.0)
                        .highRiskTransactionCount(0)
                        .userRiskLevel(UserRiskLevel.LOW)
                        .isFlagged(false)
                        .build());
        
        int totalTx = profile.getTotalTransactions() + 1;
        double txScore = tx.getRiskScore();
        double newAvgRisk = ((profile.getAverageRiskScore() * profile.getTotalTransactions()) + txScore) / totalTx;
        
        int highRiskCount = profile.getHighRiskTransactionCount();
        boolean isHighRiskTx = (tx.getRiskLevel() != null && tx.getRiskLevel().equals(RiskLevel.HIGH)) || tx.getRiskScore() >= 0.8;
        if (isHighRiskTx) {
            highRiskCount++;
        }

        UserRiskLevel level = computeUserRiskLevel(newAvgRisk);
        boolean flagged = level == UserRiskLevel.HIGH || highRiskCount >= 5;

        profile.setTotalTransactions(totalTx);
        profile.setAverageRiskScore(newAvgRisk);
        profile.setHighRiskTransactionCount(highRiskCount);
        profile.setLastTransactionTime(Instant.now());
        profile.setUserRiskLevel(level);
        profile.setIsFlagged(flagged);

        userRiskProfileRepository.save(profile);
    }

    public List<UserRiskProfile> getFlaggedUsers() {
        return userRiskProfileRepository.findByIsFlaggedTrue();
    }

    public UserRiskProfile getProfileByUserId(String userId) {
        return userRiskProfileRepository.findById(userId).orElse(null);
    }

    private UserRiskLevel computeUserRiskLevel(double avgRiskScore) {
        if (avgRiskScore < 0.4) {
            return UserRiskLevel.LOW;
        } else if (avgRiskScore < 0.7) {
            return UserRiskLevel.MEDIUM;
        } else {
            return UserRiskLevel.HIGH;
        }
    }
}