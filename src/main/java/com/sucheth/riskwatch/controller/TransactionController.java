package com.sucheth.riskwatch.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.sucheth.riskwatch.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import com.sucheth.riskwatch.dto.api.TransactionRequest;
import com.sucheth.riskwatch.dto.api.TransactionResponse;
import com.sucheth.riskwatch.dto.common.ApiResponseWrapper;
import com.sucheth.riskwatch.model.Transaction;
import com.sucheth.riskwatch.repository.TransactionRepository;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for submitting and evaluating user transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    @PostMapping
    @Operation(
        summary = "Submit a transaction for evaluation",
        description = "Evaluates a transaction for potential risk and updates the user's risk profile accordingly."
    )
    @ApiResponse(responseCode = "201", description = "Transaction evaluated and stored successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    public ResponseEntity<ApiResponseWrapper<TransactionResponse>> createTransaction(@Valid @RequestBody TransactionRequest request) {
        
        TransactionResponse response = transactionService.evaluateAndSave(request);
        ApiResponseWrapper<TransactionResponse> wrapped = ApiResponseWrapper.success(response, "Transaction evaluated successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapped);
    }

    @Operation(
        summary = "Get all transactions for a specific user",
        description = "Fetches the full transaction history for the given user ID, sorted by timestamp (newest first)."
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseWrapper<List<TransactionResponse>>> getUserTransactions(
        @Parameter(description = "Unique identifier of the user whose transactions are to be fetched")
        @PathVariable String userId) {

        List<Transaction> transactions = transactionRepository.findByUserId(userId)
            .stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .toList();

    if (transactions.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponseWrapper.error("No transactions found for user: " + userId));
    }

    List<TransactionResponse> response = transactions.stream()
        .map(TransactionResponse::from)
        .toList();

    return ResponseEntity.ok(ApiResponseWrapper.success(response, "Transactions retrieved successfully for user: " + userId
    ));
}
}