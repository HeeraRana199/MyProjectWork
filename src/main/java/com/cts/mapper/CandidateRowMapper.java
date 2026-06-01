package com.cts.mapper;

import com.cts.dto.AchievementDto;
import com.cts.dto.CandidateDto;
import com.cts.dto.CandidateScoreDto;
import com.cts.dto.CertificationDto;
import com.cts.dto.ProjectDto;
import com.cts.dto.SkillsDto;
import com.cts.entity.Achievement;
import com.cts.entity.Candidate;
import com.cts.entity.Certification;
import com.cts.entity.Project;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CandidateRowMapper {

    public CandidateDto convertToCandidateDto(Candidate candidate){
        CandidateDto candidateDto = new CandidateDto();
        candidateDto.setCognizantCandidateId(candidate.getCognizantCandidateId());
        candidateDto.setAssociateId(candidate.getAssociateId());
        candidateDto.setCandidateName(candidate.getCandidateName());
        candidateDto.setCognizantEmailID(candidate.getCognizantEmailID());
        candidateDto.setGender(candidate.getGender());
        candidateDto.setCohortCode(candidate.getCohortCode());
        candidateDto.setDeploymentLocation(candidate.getDeploymentLocation());
        candidateDto.setTrackName(candidate.getTrackName());
        candidateDto.setSl(candidate.getSl());
        candidateDto.setDoj(candidate.getDoj());


        if (candidate.getSkills() != null) {
            SkillsDto skillsDto = new SkillsDto();
            skillsDto.setSkillId(candidate.getSkills().getSkillId());
            skillsDto.setProgrammings(candidate.getSkills().getProgrammings());
            skillsDto.setFrameworks(candidate.getSkills().getFrameworks());
            skillsDto.setTools(candidate.getSkills().getTools());
            candidateDto.setSkills(skillsDto);
        } else {
            candidateDto.setSkills(null);
        }

        List<ProjectDto> projectDtoList = new ArrayList<>();
        if (candidate.getProjects() != null) {
            for(Project project: candidate.getProjects()){
                ProjectDto projectDto = new ProjectDto();
                projectDto.setProjectId(project.getProjectId());
                projectDto.setProjectName(project.getProjectName());
                projectDto.setTech(project.getTech());
                projectDto.setOutcome(project.getOutcome());
                projectDto.setRole(project.getRole());
                projectDto.setDescription(project.getDescription());
                projectDtoList.add(projectDto);
            }
        }
        candidateDto.setProjects(projectDtoList);

        List<AchievementDto> achievementDtoList = new ArrayList<>();
        if (candidate.getAchievement() != null) {
            for(Achievement achievement: candidate.getAchievement()){
                AchievementDto achievementDto = new AchievementDto();
                achievementDto.setType(achievement.getType());
                achievementDto.setTitle(achievement.getTitle());
                achievementDto.setDescription(achievement.getDescription());
                achievementDto.setAId(achievement.getAId());
                achievementDtoList.add(achievementDto);
            }
        }
        candidateDto.setAchievement(achievementDtoList);

        List<CertificationDto> certificationDtoList = new ArrayList<>();
        if (candidate.getCertificates() != null) {
            for(Certification certification: candidate.getCertificates()){
                CertificationDto certificationDto = new CertificationDto();
                certificationDto.setCertificationId(certification.getCertificationId());
                certificationDto.setCertificationName(certification.getCertificationName());
                certificationDto.setCertificationProvider(certification.getCertificationProvider());
                certificationDto.setStatus(certification.getStatus());
                certificationDtoList.add(certificationDto);
            }
        }
        candidateDto.setCertificates(certificationDtoList);

        if (candidate.getCandidateScore() != null) {
            CandidateScoreDto candidateScoreDto = new CandidateScoreDto();
            candidateScoreDto.setCandidateScoreId(candidate.getCandidateScore().getCandidateScoreId());
            candidateScoreDto.setAttendanceScore(candidate.getCandidateScore().getAttendanceScore());
            candidateScoreDto.setLanguageScore(candidate.getCandidateScore().getLanguageScore());
            candidateScoreDto.setInterimScore(candidate.getCandidateScore().getInterimScore());
            candidateScoreDto.setFinalScore(candidate.getCandidateScore().getFinalScore());
            candidateScoreDto.setInterimEvaluationFeedback(candidate.getCandidateScore().getInterimEvaluationFeedback());
            candidateScoreDto.setFinalEvaluationFeedback(candidate.getCandidateScore().getFinalEvaluationFeedback());
            candidateScoreDto.setReadiness(candidate.getCandidateScore().getReadiness());


            candidateDto.setCandidateScore(candidateScoreDto);
        } else {
            candidateDto.setCandidateScore(null);
        }

        return candidateDto;
    }
}
