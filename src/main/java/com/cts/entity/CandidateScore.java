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

    private Double attendanceScore;//double
    private String languageScore;//string
    private String interimScore;
    private String finalScore;
    private String readiness="Not Ready";

    // @Column(length = 2000)
    // private String interimEvaluationFeedback; // unused — commented out per project decision

    @Column(length = 2000)
    private String finalEvaluationFeedback;

    @OneToOne
    @JoinColumn(name="associate_id")
    private Candidate candidate;
}
