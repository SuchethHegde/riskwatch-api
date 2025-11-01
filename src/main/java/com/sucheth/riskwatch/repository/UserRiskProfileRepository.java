package com.sucheth.riskwatch.repository;

import com.sucheth.riskwatch.model.UserRiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRiskProfileRepository extends JpaRepository<UserRiskProfile, String> {
    List<UserRiskProfile> findByIsFlaggedTrue();
}