package com.sucheth.riskwatch.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.sucheth.riskwatch.model.enums.RiskLevel;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String userId;
    private Double amount;
    private Instant timestamp;
    private String deviceId;
    private String location;

    private Integer riskScore;
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @ElementCollection
    private List<String> reasons = new ArrayList<>();
}