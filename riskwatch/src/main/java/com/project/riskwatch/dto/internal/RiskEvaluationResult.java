package com.project.riskwatch.dto.internal;

import java.util.List;
import lombok.AllArgsConstructor;
package com.project.riskwatch.dto.internal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskEvaluationResult {
    private int score;
    private RiskLevel level;
    private List<String> reasons;
}

@Data
@AllArgsConstructor
public class RiskEvaluationResult {
    private int score;
    private RiskLevel level;
    private List<String> reasons;
}