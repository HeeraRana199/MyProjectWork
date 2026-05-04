package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateScoreDto {
    private Integer candidateScoreId;

    private Double performanceScore;
    private Double attendanceScore;
    private String languageScore;
    private String interimScore;
    private String finalScore;
}
