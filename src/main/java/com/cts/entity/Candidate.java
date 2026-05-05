package com.cts.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Candidate {

    @Id
    @Column(name = "cognizant_candidate_id", unique = true)
    private Integer cognizantCandidateId; // ✅ Excel value

    private Integer associateId;
    private String candidateName;
    private String cognizantEmailID;
    private String gender;
    private String cohortCode;
    private String deploymentLocation;
    private String trackName;
    private LocalDate doj;

    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL)
    private CandidateScore candidateScore;

    @OneToMany(mappedBy = "candidate" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certification> certificates;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Achievement> achievement;

    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Skills skills;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects;


    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private User user;



}
