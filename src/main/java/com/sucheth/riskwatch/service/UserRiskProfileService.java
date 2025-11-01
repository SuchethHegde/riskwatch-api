package com.sucheth.riskwatch.service;

import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.model.UserRiskProfile;
import com.sucheth.riskwatch.repository.UserRiskProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRiskProfileService {
    private final UserRiskProfileRepository UserRiskProfileRepository;

    @Transactional
    public void updateUserRiskProfile(Transaction tx) {
        UserRiskProfile profile = UserRiskProfileRepository.findById(tx.getUserId())
                .orElse(UserRiskProfile.builder()
                        .userId(tx.getUserId())
                        .totalTransactions(0)
                        .averageRiskScore(0.0)
                        .highRiskTransactionCount(0)
                        .isFlagged(false)
                        .build());
        
        int totalTx = profile.getTotalTransactions() + 1;
        double newAvgRisk = ((profile.getAverageRiskScore() * profile.getTotalTransactions()) + tx.getRiskScore()) / totalTx;
        int highRiskCount = profile.getHighRiskTransactionCount();
        if (tx.getRiskLevel().name().equals("HIGH")) { // Assuming risk score > 7 is high risk
            highRiskCount++;
        }
        String level = computeUserRiskLevel(newAvgRisk);
        boolean flagged = level.equals("HIGH") || highRiskCount >= 5;

        profile.setTotalTransactions(totalTx);
        profile.setAverageRiskScore(newAvgRisk);
        profile.setHighRiskTransactionCount(highRiskCount);
        profile.setLastTransactionTime(Instant.now());
        profile.setUserRiskLevel(level);
        profile.setIsFlagged(flagged);

        UserRiskProfileRepository.save(profile);
    }

    public List<UserRiskProfile> getFlaggedUsers() {
        return UserRiskProfileRepository.findByIsFlaggedTrue();
    }

    public UserRiskProfile getProfileByUserId(String userId) {
        return UserRiskProfileRepository.findById(userId).orElse(null);
    }

    private String computeUserRiskLevel(double avgRiskScore) {
        if (avgRiskScore < 40) {
            return "LOW";
        } else if (avgRiskScore < 70) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }
}