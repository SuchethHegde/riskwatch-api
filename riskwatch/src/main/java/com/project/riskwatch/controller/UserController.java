package com.project.riskwatch.controller;

import com.project.riskwatch.dto.api.UserRiskProfileResponse;
import com.project.riskwatch.model.UserRiskProfile;
import com.project.riskwatch.service.UserRiskProfileService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRiskProfileService userRiskProfileService;

    @GetMapping("/flagged")
    public ResponseEntity<List<UserRiskProfileResponse>> getFlaggedUsers() {
        List<UserRiskProfile> flaggedUsers = userRiskProfileService.getFlaggedUsers();
        List<UserRiskProfileResponse> response = flaggedUsers.stream()
                .map(UserRiskProfileResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/risk-profile")
    public ResponseEntity<UserRiskProfileResponse> getUserRiskProfile(@PathVariable String userId) {
        UserRiskProfile profile = userRiskProfileService.getProfileByUserId(userId);
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(UserRiskProfileResponse.from(profile));
    }
}