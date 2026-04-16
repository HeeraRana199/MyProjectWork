package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Achievement;
import com.cts.repository.AchievementRepository;
import com.cts.repository.CandidateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class AchievementService {

    private CandidateRepository candidateRepository;
    private AchievementRepository achievementRepository;

    //Create achievement
    public Achievement addAchievement(Achievement achievement, Integer candidateId){
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(()-> new RuntimeException("Candidate not found for Add Achievement!!"));

        achievement.setCandidate(candidate);
        return achievementRepository.save(achievement);
    }

    //Fetch achievement using achievementId
    public Achievement getAchievement(Integer achievementId){
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(()-> new RuntimeException("Achievement not exist for this achievementID"));

        return achievement;
    }

    //Update achievement using achievementId
    public Achievement updateAchievement(Achievement achievement, Integer achievementId){
        Achievement existingAchievement = achievementRepository.findById(achievementId)
                .orElseThrow(()-> new RuntimeException("This achievement not found for update"));

        if(achievement.getTitle() != null){
            existingAchievement.setTitle(achievement.getTitle());
        }
        if(achievement.getDescription() != null){
            existingAchievement.setDescription(achievement.getDescription());
        }

        return achievementRepository.save(existingAchievement);
    }

    //Delete achievement using the achievementId
    public void deleteAchievement(Integer achievementId){
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(()-> new RuntimeException("Achievement not found to delete!!"));

        achievementRepository.delete(achievement);
    }
}
