package com.sucheth.riskwatch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.model.UserRiskProfile;
import com.sucheth.riskwatch.model.enums.RiskLevel;
import com.sucheth.riskwatch.model.enums.UserRiskLevel;
import com.sucheth.riskwatch.repository.UserRiskProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRiskProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserRiskProfileService.class);

    private final UserRiskProfileRepository userRiskProfileRepository;

    @Transactional
    public void updateUserRiskProfile(Transaction tx) {
        logger.debug("Updating risk profile for user: userId={}, transactionId={}", 
                tx.getUserId(), tx.getTransactionId());

        UserRiskProfile profile = userRiskProfileRepository.findById(tx.getUserId())
                .orElse(UserRiskProfile.builder()
                        .userId(tx.getUserId())
                        .totalTransactions(0)
                        .averageRiskScore(0.0)
                        .highRiskTransactionCount(0)
                        .userRiskLevel(UserRiskLevel.LOW)
                        .isFlagged(false)
                        .build());
        
        boolean isNewProfile = !userRiskProfileRepository.existsById(tx.getUserId());

        
        int currentTotalTx = profile.getTotalTransactions() != null ? profile.getTotalTransactions() : 0;
        int totalTx = currentTotalTx + 1;
        double txScore = tx.getRiskScore();
        double currentAvgRisk = profile.getAverageRiskScore() != null ? profile.getAverageRiskScore() : 0.0;
        double newAvgRisk = ((currentAvgRisk * currentTotalTx) + txScore) / totalTx;
        
        int highRiskCount = profile.getHighRiskTransactionCount() != null ? profile.getHighRiskTransactionCount() : 0;
        boolean isHighRiskTx = (tx.getRiskLevel() != null && tx.getRiskLevel().equals(RiskLevel.HIGH)) || tx.getRiskScore() >= 0.8;
        if (isHighRiskTx) {
            highRiskCount++;
            logger.warn("High risk transaction for user: userId={}, transactionId={}, riskScore={}", 
                    tx.getUserId(), tx.getTransactionId(), tx.getRiskScore());
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
        
        if (isNewProfile) {
            logger.info("Created new risk profile: userId={}", tx.getUserId());
        } else {
            logger.debug("Updated risk profile: userId={}, totalTransactions={}, avgRiskScore={}, userRiskLevel={}, flagged={}", 
                    tx.getUserId(), totalTx, newAvgRisk, level, flagged);
        }
        
        if (flagged && !isNewProfile) {
            logger.info("User flagged as high risk: userId={}, userRiskLevel={}, highRiskCount={}", 
                    tx.getUserId(), level, highRiskCount);
        }
    }

    public List<UserRiskProfile> getFlaggedUsers() {
        logger.info("Fetching flagged users");
        List<UserRiskProfile> flaggedUsers = userRiskProfileRepository.findByIsFlaggedTrue();
        logger.debug("Found {} flagged users", flaggedUsers.size());
        return flaggedUsers;
    }

    public Page<UserRiskProfile> getFlaggedUsers(Pageable pageable) {
        logger.info("Fetching flagged users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserRiskProfile> flaggedUsersPage = userRiskProfileRepository.findByIsFlaggedTrue(pageable);
        logger.debug("Found {} flagged users (total: {})", flaggedUsersPage.getNumberOfElements(), flaggedUsersPage.getTotalElements());
        return flaggedUsersPage;
    }

    public UserRiskProfile getProfileByUserId(String userId) {
        logger.debug("Fetching risk profile for user: userId={}", userId);
        UserRiskProfile profile = userRiskProfileRepository.findById(userId).orElse(null);
        if (profile == null) {
            logger.warn("Risk profile not found for user: userId={}", userId);
        }
        return profile;
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