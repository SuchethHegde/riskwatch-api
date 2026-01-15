package com.sucheth.riskwatch.controller;

import com.sucheth.riskwatch.dto.api.UserRiskProfileResponse;
import com.sucheth.riskwatch.dto.common.ApiResponseWrapper;
import com.sucheth.riskwatch.model.UserRiskProfile;
import com.sucheth.riskwatch.service.UserRiskProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Risk Profile API", description = "Endpoints for fetching user-level risk information")
public class UserController {

    private final UserRiskProfileService userRiskProfileService;

    @Operation(summary = "List all flagged users", description = "Retrieves all users currently marked as high-risk or flagged")
    @GetMapping("/flagged")
    public ResponseEntity<ApiResponseWrapper<List<UserRiskProfileResponse>>> getFlaggedUsers() {

        List<UserRiskProfile> flaggedUsers = userRiskProfileService.getFlaggedUsers();

        if(flaggedUsers.isEmpty()) {
            return ResponseEntity.ok(ApiResponseWrapper.success(List.of(), "No flagged users found"));
        }

        List<UserRiskProfileResponse> response = flaggedUsers.stream()
                .map(UserRiskProfileResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseWrapper.success(response, "Flagged users retrieved successfully"));
    }

    @Operation(summary = "Get a user's risk profile", description = "Fetches the latest risk profile for a given userId")
    @GetMapping("/{userId}/risk-profile")
    public ResponseEntity<ApiResponseWrapper<UserRiskProfileResponse>> getUserRiskProfile(
        @Parameter(description = "Unique identifier of the user")
        @PathVariable String userId) {

        UserRiskProfile profile = userRiskProfileService.getProfileByUserId(userId);
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseWrapper.error("User not found: " + userId));
        }

        UserRiskProfileResponse response = UserRiskProfileResponse.from(profile);

        return ResponseEntity.ok(
            ApiResponseWrapper.success(response, "User risk profile retrieved successfully.")
        );
    }
}