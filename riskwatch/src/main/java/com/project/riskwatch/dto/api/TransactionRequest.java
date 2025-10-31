package com.project.riskwatch.dto.api;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class TransactionRequest {
    @NotBlank private String transactionId;
    @NotBlank private String userId;
    @NotBlank private Double amount;
    @NotNull private Instant timestamp;
    private String deviceId;
    private String location;
}