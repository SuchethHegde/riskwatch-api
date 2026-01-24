package com.sucheth.riskwatch.controller;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sucheth.riskwatch.dto.api.UserRiskProfileResponse;
import com.sucheth.riskwatch.dto.common.ApiResponseWrapper;
import com.sucheth.riskwatch.model.UserRiskProfile;
import com.sucheth.riskwatch.service.UserRiskProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Risk Profile API", description = "Endpoints for fetching user-level risk information")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRiskProfileService userRiskProfileService;

    @Operation(summary = "List all flagged users", description = "Retrieves all users currently marked as high-risk or flagged with pagination support")
    @GetMapping("/flagged")
    public ResponseEntity<ApiResponseWrapper<Page<UserRiskProfileResponse>>> getFlaggedUsers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Sort field", example = "averageRiskScore")
            @RequestParam(defaultValue = "averageRiskScore") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        logger.info("Fetching flagged users: page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);

        // Validate sort direction
        if (!sortDir.equalsIgnoreCase("ASC") && !sortDir.equalsIgnoreCase("DESC")) {
            throw new IllegalArgumentException("Sort direction must be ASC or DESC");
        }

        // Validate sort field to prevent injection (whitelist approach)
        Set<String> allowedSortFields = Set.of(
            "averageRiskScore", "totalTransactions", "highRiskTransactionCount",
            "lastTransactionTime", "userRiskLevel"
        );
        
        if (allowedSortFields.stream().noneMatch(f -> f.equalsIgnoreCase(sortBy))) {
            throw new IllegalArgumentException("Invalid sort field. Allowed fields: " + String.join(", ", allowedSortFields));
        }
        

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserRiskProfile> flaggedUsersPage = userRiskProfileService.getFlaggedUsers(pageable);

        Page<UserRiskProfileResponse> responsePage = flaggedUsersPage.map(UserRiskProfileResponse::from);

        if(responsePage.isEmpty()) {
            logger.debug("No flagged users found");
        } else {
            logger.info("Retrieved {} flagged users (page {} of {})", 
                    responsePage.getNumberOfElements(), page, responsePage.getTotalPages());
        }
        
        return ResponseEntity.ok(ApiResponseWrapper.success(responsePage, "Flagged users retrieved successfully"));
    }

    @Operation(summary = "Get a user's risk profile", description = "Fetches the latest risk profile for a given userId")
    @GetMapping("/{userId}/risk-profile")
    public ResponseEntity<ApiResponseWrapper<UserRiskProfileResponse>> getUserRiskProfile(
        @Parameter(description = "Unique identifier of the user")
        @PathVariable String userId) {

        logger.info("Fetching risk profile for user: userId={}", userId);

        UserRiskProfile profile = userRiskProfileService.getProfileByUserId(userId);
        if (profile == null) {
            logger.warn("Risk profile not found for user: userId={}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseWrapper.error("User not found: " + userId));
        }

        UserRiskProfileResponse response = UserRiskProfileResponse.from(profile);

        logger.debug("Risk profile retrieved for user: userId={}, riskLevel={}, flagged={}", 
                userId, profile.getUserRiskLevel(), profile.getIsFlagged());

        return ResponseEntity.ok(
            ApiResponseWrapper.success(response, "User risk profile retrieved successfully.")
        );
    }
}