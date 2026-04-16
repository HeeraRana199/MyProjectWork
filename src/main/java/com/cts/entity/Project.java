package com.cts.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    private String projectName;
    private String tech;
    private String outcome;
    private String role;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;
}
