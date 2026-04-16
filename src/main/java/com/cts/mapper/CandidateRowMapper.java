package com.cts.mapper;

import com.cts.dto.AchievementDto;
import com.cts.dto.CandidateDto;
import com.cts.dto.ProjectDto;
import com.cts.dto.SkillsDto;
import com.cts.entity.Achievement;
import com.cts.entity.Candidate;
import com.cts.entity.Project;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CandidateRowMapper {

    public CandidateDto convertToCandidateDto(Candidate candidate){
        CandidateDto candidateDto = new CandidateDto();
        candidateDto.setCognizantCandidateId(candidate.getCognizantCandidateId());
        candidateDto.setCandidateName(candidate.getCandidateName());

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
                projectDto.setRole(project.getRole());
                projectDto.setName(project.getProjectName());
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
        return candidateDto;
    }
}
