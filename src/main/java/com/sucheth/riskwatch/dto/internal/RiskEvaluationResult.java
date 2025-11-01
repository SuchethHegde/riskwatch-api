package com.sucheth.riskwatch.dto.internal;

import java.util.List;

import com.sucheth.riskwatch.model.enums.RiskLevel;

import lombok.AllArgsConstructor;

import lombok.Data;

@Data
@AllArgsConstructor
public class RiskEvaluationResult {
    private double score;
    private RiskLevel level;
    private List<String> reasons;
}