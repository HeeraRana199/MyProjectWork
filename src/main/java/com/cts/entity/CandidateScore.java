package com.cts.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CandidateScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer candidateScoreId;

    private Double performanceScore;//double
    private Double attendanceScore;//double
    private String languageScore;//string
    private String interimScore;
    private String finalScore;

    @OneToOne
    @JoinColumn(name="candidate_id")
    private Candidate candidate;
}
