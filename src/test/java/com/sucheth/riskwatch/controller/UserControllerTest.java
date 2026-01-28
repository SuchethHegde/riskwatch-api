package com.sucheth.riskwatch.controller;

import com.sucheth.riskwatch.model.UserRiskProfile;
import com.sucheth.riskwatch.model.enums.UserRiskLevel;
import com.sucheth.riskwatch.service.UserRiskProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRiskProfileService userRiskProfileService;

    @Test
    void getUserRiskProfile_UserExists_ReturnsOk() throws Exception {
        // Given
        String userId = "user-001";
        UserRiskProfile profile = UserRiskProfile.builder()
                .userId(userId)
                .totalTransactions(10)
                .averageRiskScore(0.4)
                .highRiskTransactionCount(2)
                .lastTransactionTime(Instant.now())
                .userRiskLevel(UserRiskLevel.MEDIUM)
                .isFlagged(false)
                .build();

        when(userRiskProfileService.getProfileByUserId(userId)).thenReturn(profile);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}/risk-profile", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.totalTransactions").value(10))
                .andExpect(jsonPath("$.data.averageRiskScore").value(0.4))
                .andExpect(jsonPath("$.data.userRiskLevel").value("MEDIUM"));
    }

    @Test
    void getUserRiskProfile_UserNotExists_ReturnsNotFound() throws Exception {
        // Given
        String userId = "user-999";
        when(userRiskProfileService.getProfileByUserId(userId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}/risk-profile", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found: user-999"));
    }

    @Test
    void getFlaggedUsers_UsersExist_ReturnsOk() throws Exception {
        // Given
        List<UserRiskProfile> flaggedUsers = List.of(
                UserRiskProfile.builder()
                        .userId("user-001")
                        .totalTransactions(10)
                        .averageRiskScore(0.8)
                        .highRiskTransactionCount(5)
                        .lastTransactionTime(Instant.now())
                        .userRiskLevel(UserRiskLevel.HIGH)
                        .isFlagged(true)
                        .build(),
                UserRiskProfile.builder()
                        .userId("user-002")
                        .totalTransactions(15)
                        .averageRiskScore(0.75)
                        .highRiskTransactionCount(6)
                        .lastTransactionTime(Instant.now())
                        .userRiskLevel(UserRiskLevel.HIGH)
                        .isFlagged(true)
                        .build()
        );

        when(userRiskProfileService.getFlaggedUsers()).thenReturn(flaggedUsers);

        // When & Then
        mockMvc.perform(get("/api/v1/users/flagged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].userId").value("user-001"))
                .andExpect(jsonPath("$.data[0].isFlagged").value(true));
    }

    @Test
    void getFlaggedUsers_NoFlaggedUsers_ReturnsEmptyList() throws Exception {
        // Given
        when(userRiskProfileService.getFlaggedUsers()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/users/flagged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.message").value("No flagged users found"));
    }
}
