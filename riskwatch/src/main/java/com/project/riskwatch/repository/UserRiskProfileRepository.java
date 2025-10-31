package com.project.riskwatch.repository;

import com.project.riskwatch.model.UserRiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRiskProfileRepository extends JpaRepository<UserRiskProfile, String> {
    List<UserRiskProfile> findByIsFlaggedTrue();
}