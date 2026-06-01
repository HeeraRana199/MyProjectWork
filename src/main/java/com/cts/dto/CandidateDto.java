package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateDto {

    private Integer cognizantCandidateId; // ✅ Excel value

    private Integer associateId;
    private String candidateName;
    private String cognizantEmailID;
    private String gender;
    private String cohortCode;
    private String deploymentLocation;
    private String trackName;
    private String sl;
    private LocalDate doj;

    private List<CertificationDto> certificates;
    private List<AchievementDto> achievement;
    private SkillsDto skills;
    private List<ProjectDto> projects;
    private CandidateScoreDto candidateScore;
}


