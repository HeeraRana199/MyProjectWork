package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateScoreDto {
    private Integer candidateScoreId;

    private Double attendanceScore;
    private String languageScore;
    private String interimScore;
    private String finalScore;
    // private String interimEvaluationFeedback; // unused — commented out per project decision
    private String finalEvaluationFeedback;
    private String readiness;
}
