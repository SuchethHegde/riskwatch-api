package com.project.riskwatch.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.ElementCollection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


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