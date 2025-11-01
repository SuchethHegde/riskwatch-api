package com.sucheth.riskwatch.dto.api;

import lombok.Data;
import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class TransactionRequest {
    
    @NotBlank
    private String transactionId;
    
    @NotBlank
    private String userId;
    
    @NotNull
    @Positive
    private Double amount;
    
    @NotNull
    private Instant timestamp;
    
    private String deviceId;
    private String location;
}