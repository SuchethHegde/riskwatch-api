package com.sucheth.riskwatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sucheth.riskwatch.dto.api.TransactionRequest;
import com.sucheth.riskwatch.dto.api.TransactionResponse;
import com.sucheth.riskwatch.model.enums.RiskLevel;
import com.sucheth.riskwatch.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTransaction_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setTransactionId("tx-001");
        request.setUserId("user-001");
        request.setAmount(50000.0);
        request.setTimestamp(Instant.now());

        TransactionResponse response = TransactionResponse.builder()
                .transactionId("tx-001")
                .userId("user-001")
                .riskScore(0.2)
                .riskLevel(RiskLevel.LOW)
                .reasons(Collections.emptyList())
                .evaluatedAt(Instant.now())
                .build();

        when(transactionService.evaluateAndSave(any(TransactionRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionId").value("tx-001"))
                .andExpect(jsonPath("$.data.userId").value("user-001"))
                .andExpect(jsonPath("$.data.riskScore").value(0.2))
                .andExpect(jsonPath("$.data.riskLevel").value("LOW"));
    }

    @Test
    void createTransaction_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - Missing required fields
        TransactionRequest request = new TransactionRequest();
        request.setTransactionId(""); // Empty transactionId

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_NegativeAmount_ReturnsBadRequest() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setTransactionId("tx-002");
        request.setUserId("user-002");
        request.setAmount(-100.0); // Negative amount

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserTransactions_UserExists_ReturnsOk() throws Exception {
        // Given
        String userId = "user-001";
        List<TransactionResponse> responses = List.of(
                TransactionResponse.builder()
                        .transactionId("tx-001")
                        .userId(userId)
                        .riskScore(0.2)
                        .riskLevel(RiskLevel.LOW)
                        .reasons(Collections.emptyList())
                        .evaluatedAt(Instant.now())
                        .build()
        );

        when(transactionService.getUserTransactions(userId)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].transactionId").value("tx-001"));
    }

    @Test
    void getUserTransactions_NoTransactions_ReturnsNotFound() throws Exception {
        // Given
        String userId = "user-999";
        when(transactionService.getUserTransactions(userId)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/user/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
