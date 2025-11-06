package com.sucheth.riskwatch.controller;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sucheth.riskwatch.dto.common.ApiResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/v1/health")
@Tag(name = "Health", description = "API health and readiness checks")
public class HealthController {
    
    @GetMapping
    @Operation(summary = "Health Check", description = "Returns a simple OK response to confirm that the API is running")
    public ResponseEntity<ApiResponseWrapper<String>> healthCheck() {
        String statusMessage = "RiskWatch API is up and running at " + Instant.now();
        return ResponseEntity.ok(ApiResponseWrapper.success(statusMessage, "Health check passed"));
    }
}