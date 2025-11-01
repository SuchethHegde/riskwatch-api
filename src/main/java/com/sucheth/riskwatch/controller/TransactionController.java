package com.sucheth.riskwatch.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.sucheth.riskwatch.service.TransactionService;
import com.sucheth.riskwatch.dto.api.TransactionRequest;
import com.sucheth.riskwatch.dto.api.TransactionResponse;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.evaluateAndSave(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}