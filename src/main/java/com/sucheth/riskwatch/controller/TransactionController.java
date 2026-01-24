package com.sucheth.riskwatch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import com.sucheth.riskwatch.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import com.sucheth.riskwatch.dto.api.TransactionRequest;
import com.sucheth.riskwatch.dto.api.TransactionResponse;
import com.sucheth.riskwatch.dto.common.ApiResponseWrapper;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for submitting and evaluating user transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    @PostMapping
    @Operation(
        summary = "Submit a transaction for evaluation",
        description = "Evaluates a transaction for potential risk and updates the user's risk profile accordingly."
    )
    @ApiResponse(responseCode = "201", description = "Transaction evaluated and stored successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    public ResponseEntity<ApiResponseWrapper<TransactionResponse>> createTransaction(@Valid @RequestBody TransactionRequest request) {
        logger.info("Received transaction evaluation request: transactionId={}, userId={}", 
                request.getTransactionId(), request.getUserId());
        
        TransactionResponse response = transactionService.evaluateAndSave(request);
        ApiResponseWrapper<TransactionResponse> wrapped = ApiResponseWrapper.success(response, "Transaction evaluated successfully.");
        
        logger.info("Transaction evaluation completed: transactionId={}, riskLevel={}", 
                request.getTransactionId(), response.getRiskLevel());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapped);
    }

    @Operation(
        summary = "Get all transactions for a specific user",
        description = "Fetches the transaction history for the given user ID with pagination support, sorted by timestamp (newest first)."
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseWrapper<Page<TransactionResponse>>> getUserTransactions(
        @Parameter(description = "Unique identifier of the user whose transactions are to be fetched")
        @PathVariable String userId,
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        logger.info("Fetching transactions for user: userId={}, page={}, size={}", userId, page, size);
        
        Page<TransactionResponse> response = transactionService.getUserTransactions(userId, page, size);

        if (response.isEmpty()) {
            logger.warn("No transactions found for user: userId={}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseWrapper.error("No transactions found for user: " + userId));
        }

        logger.debug("Retrieved {} transactions for user: userId={} (page {} of {})", 
                response.getNumberOfElements(), userId, page, response.getTotalPages());
        return ResponseEntity.ok(ApiResponseWrapper.success(response, "Transactions retrieved successfully for user: " + userId));
    }
}