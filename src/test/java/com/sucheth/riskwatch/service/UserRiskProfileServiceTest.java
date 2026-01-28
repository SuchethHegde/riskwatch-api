package com.sucheth.riskwatch.service;

import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.model.UserRiskProfile;
import com.sucheth.riskwatch.model.enums.RiskLevel;
import com.sucheth.riskwatch.model.enums.UserRiskLevel;
import com.sucheth.riskwatch.repository.UserRiskProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRiskProfileServiceTest {

    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;

    @InjectMocks
    private UserRiskProfileService userRiskProfileService;

    @Test
    void updateUserRiskProfile_NewUser_CreatesProfile() {
        // Given
        Transaction tx = Transaction.builder()
                .transactionId("tx-001")
                .userId("user-001")
                .amount(50000.0)
                .timestamp(Instant.now())
                .riskScore(0.3)
                .riskLevel(RiskLevel.LOW)
                .reasons(Collections.emptyList())
                .build();

        when(userRiskProfileRepository.findById("user-001")).thenReturn(Optional.empty());
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userRiskProfileService.updateUserRiskProfile(tx);

        // Then
        ArgumentCaptor<UserRiskProfile> profileCaptor = ArgumentCaptor.forClass(UserRiskProfile.class);
        verify(userRiskProfileRepository).save(profileCaptor.capture());

        UserRiskProfile savedProfile = profileCaptor.getValue();
        assertEquals("user-001", savedProfile.getUserId());
        assertEquals(1, savedProfile.getTotalTransactions());
        assertEquals(0.3, savedProfile.getAverageRiskScore(), 0.01);
        assertEquals(0, savedProfile.getHighRiskTransactionCount());
        assertEquals(UserRiskLevel.LOW, savedProfile.getUserRiskLevel());
        assertFalse(savedProfile.getIsFlagged());
    }

    @Test
    void updateUserRiskProfile_ExistingUser_UpdatesProfile() {
        // Given
        UserRiskProfile existingProfile = UserRiskProfile.builder()
                .userId("user-002")
                .totalTransactions(5)
                .averageRiskScore(0.4)
                .highRiskTransactionCount(1)
                .lastTransactionTime(Instant.now().minusSeconds(3600))
                .userRiskLevel(UserRiskLevel.MEDIUM)
                .isFlagged(false)
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("tx-002")
                .userId("user-002")
                .amount(50000.0)
                .timestamp(Instant.now())
                .riskScore(0.5)
                .riskLevel(RiskLevel.MEDIUM)
                .reasons(Collections.emptyList())
                .build();

        when(userRiskProfileRepository.findById("user-002")).thenReturn(Optional.of(existingProfile));
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userRiskProfileService.updateUserRiskProfile(tx);

        // Then
        ArgumentCaptor<UserRiskProfile> profileCaptor = ArgumentCaptor.forClass(UserRiskProfile.class);
        verify(userRiskProfileRepository).save(profileCaptor.capture());

        UserRiskProfile savedProfile = profileCaptor.getValue();
        assertEquals(6, savedProfile.getTotalTransactions());
        // Average should be: (0.4 * 5 + 0.5) / 6 = 2.5 / 6 = 0.416...
        assertEquals(0.416, savedProfile.getAverageRiskScore(), 0.01);
        assertEquals(1, savedProfile.getHighRiskTransactionCount()); // Still 1, not high risk
    }

    @Test
    void updateUserRiskProfile_HighRiskTransaction_IncrementsCount() {
        // Given
        UserRiskProfile existingProfile = UserRiskProfile.builder()
                .userId("user-003")
                .totalTransactions(2)
                .averageRiskScore(0.3)
                .highRiskTransactionCount(0)
                .lastTransactionTime(Instant.now().minusSeconds(3600))
                .userRiskLevel(UserRiskLevel.LOW)
                .isFlagged(false)
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("tx-003")
                .userId("user-003")
                .amount(50000.0)
                .timestamp(Instant.now())
                .riskScore(0.85) // High risk score
                .riskLevel(RiskLevel.HIGH)
                .reasons(Collections.emptyList())
                .build();

        when(userRiskProfileRepository.findById("user-003")).thenReturn(Optional.of(existingProfile));
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userRiskProfileService.updateUserRiskProfile(tx);

        // Then
        ArgumentCaptor<UserRiskProfile> profileCaptor = ArgumentCaptor.forClass(UserRiskProfile.class);
        verify(userRiskProfileRepository).save(profileCaptor.capture());

        UserRiskProfile savedProfile = profileCaptor.getValue();
        assertEquals(1, savedProfile.getHighRiskTransactionCount());
    }

    @Test
    void updateUserRiskProfile_HighAverageRisk_FlagsUser() {
        // Given
        UserRiskProfile existingProfile = UserRiskProfile.builder()
                .userId("user-004")
                .totalTransactions(10)
                .averageRiskScore(0.65)
                .highRiskTransactionCount(2)
                .lastTransactionTime(Instant.now().minusSeconds(3600))
                .userRiskLevel(UserRiskLevel.MEDIUM)
                .isFlagged(false)
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("tx-004")
                .userId("user-004")
                .amount(50000.0)
                .timestamp(Instant.now())
                .riskScore(0.85) // High risk
                .riskLevel(RiskLevel.HIGH)
                .reasons(Collections.emptyList())
                .build();

        when(userRiskProfileRepository.findById("user-004")).thenReturn(Optional.of(existingProfile));
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userRiskProfileService.updateUserRiskProfile(tx);

        // Then
        ArgumentCaptor<UserRiskProfile> profileCaptor = ArgumentCaptor.forClass(UserRiskProfile.class);
        verify(userRiskProfileRepository).save(profileCaptor.capture());

        UserRiskProfile savedProfile = profileCaptor.getValue();
        assertTrue(savedProfile.getAverageRiskScore() >= 0.7 || savedProfile.getUserRiskLevel() == UserRiskLevel.HIGH);
    }

    @Test
    void updateUserRiskProfile_FiveHighRiskTransactions_FlagsUser() {
        // Given
        UserRiskProfile existingProfile = UserRiskProfile.builder()
                .userId("user-005")
                .totalTransactions(10)
                .averageRiskScore(0.4)
                .highRiskTransactionCount(4) // Already has 4
                .lastTransactionTime(Instant.now().minusSeconds(3600))
                .userRiskLevel(UserRiskLevel.MEDIUM)
                .isFlagged(false)
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("tx-005")
                .userId("user-005")
                .amount(50000.0)
                .timestamp(Instant.now())
                .riskScore(0.85)
                .riskLevel(RiskLevel.HIGH)
                .reasons(Collections.emptyList())
                .build();

        when(userRiskProfileRepository.findById("user-005")).thenReturn(Optional.of(existingProfile));
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userRiskProfileService.updateUserRiskProfile(tx);

        // Then
        ArgumentCaptor<UserRiskProfile> profileCaptor = ArgumentCaptor.forClass(UserRiskProfile.class);
        verify(userRiskProfileRepository).save(profileCaptor.capture());

        UserRiskProfile savedProfile = profileCaptor.getValue();
        assertEquals(5, savedProfile.getHighRiskTransactionCount());
        assertTrue(savedProfile.getIsFlagged()); // Should be flagged after 5 high-risk transactions
    }

    @Test
    void getFlaggedUsers_ReturnsFlaggedUsers() {
        // Given
        List<UserRiskProfile> flaggedUsers = List.of(
                UserRiskProfile.builder()
                        .userId("user-001")
                        .isFlagged(true)
                        .build(),
                UserRiskProfile.builder()
                        .userId("user-002")
                        .isFlagged(true)
                        .build()
        );

        when(userRiskProfileRepository.findByIsFlaggedTrue()).thenReturn(flaggedUsers);

        // When
        List<UserRiskProfile> result = userRiskProfileService.getFlaggedUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(UserRiskProfile::getIsFlagged));
    }

    @Test
    void getProfileByUserId_UserExists_ReturnsProfile() {
        // Given
        UserRiskProfile profile = UserRiskProfile.builder()
                .userId("user-001")
                .totalTransactions(5)
                .averageRiskScore(0.4)
                .build();

        when(userRiskProfileRepository.findById("user-001")).thenReturn(Optional.of(profile));

        // When
        UserRiskProfile result = userRiskProfileService.getProfileByUserId("user-001");

        // Then
        assertNotNull(result);
        assertEquals("user-001", result.getUserId());
    }

    @Test
    void getProfileByUserId_UserNotExists_ReturnsNull() {
        // Given
        when(userRiskProfileRepository.findById("user-999")).thenReturn(Optional.empty());

        // When
        UserRiskProfile result = userRiskProfileService.getProfileByUserId("user-999");

        // Then
        assertNull(result);
    }
}
