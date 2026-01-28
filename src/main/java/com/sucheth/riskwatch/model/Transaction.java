package com.sucheth.riskwatch.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(unique = true, nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Instant timestamp;

    private String deviceId;
    private String location;

    @Column(nullable = false)
    private double riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @ElementCollection
    @CollectionTable(name = "transaction_reasons", joinColumns = @JoinColumn(name = "transaction_id"))
    @Column(name = "reason")
    @Builder.Default
    private List<String> reasons = new ArrayList<>();
}