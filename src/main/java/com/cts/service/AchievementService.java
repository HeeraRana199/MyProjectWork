package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Achievement;
import com.cts.repository.AchievementRepository;
import com.cts.repository.CandidateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@AllArgsConstructor
public class AchievementService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);
    private CandidateRepository candidateRepository;
    private AchievementRepository achievementRepository;

    //Create achievement
    public Achievement addAchievement(Achievement achievement, Integer associateId){
        logger.info("Adding achievement for associateId: {}, Title: {}", associateId, achievement.getTitle());
        try {
            Candidate candidate = candidateRepository.findById(associateId)
                    .orElseThrow(()-> new RuntimeException("Candidate not found for Add Achievement!!"));

            achievement.setCandidate(candidate);
            Achievement savedAchievement = achievementRepository.save(achievement);
            logger.debug("Achievement saved successfully with ID: {} for associateId: {}", savedAchievement.getAId(), associateId);
            return savedAchievement;
        } catch (Exception e) {
            logger.error("Error while adding achievement for associateId: {}", associateId, e);
            throw e;
        }
    }

    //Fetch achievement using achievementId
    public Achievement getAchievement(Integer achievementId){
        logger.debug("Fetching achievement with ID: {}", achievementId);
        try {
            Achievement achievement = achievementRepository.findById(achievementId)
                    .orElseThrow(()-> new RuntimeException("Achievement not exist for this achievementID"));

            logger.debug("Successfully retrieved achievement with ID: {}", achievementId);
            return achievement;
        } catch (Exception e) {
            logger.error("Error while fetching achievement with ID: {}", achievementId, e);
            throw e;
        }
    }

    //Update achievement using achievementId
    public Achievement updateAchievement(Achievement achievement, Integer achievementId){
        logger.info("Updating achievement with ID: {}", achievementId);
        try {
            Achievement existingAchievement = achievementRepository.findById(achievementId)
                    .orElseThrow(()-> new RuntimeException("This achievement not found for update"));

            if(achievement.getType() != null){
                logger.debug("Updating achievement type for ID: {} to: {}", achievementId, achievement.getType());
                existingAchievement.setType(achievement.getType());
            }
            if(achievement.getTitle() != null){
                logger.debug("Updating achievement title for ID: {} to: {}", achievementId, achievement.getTitle());
                existingAchievement.setTitle(achievement.getTitle());
            }
            if(achievement.getDescription() != null){
                logger.debug("Updating achievement description for ID: {}", achievementId);
                existingAchievement.setDescription(achievement.getDescription());
            }

            Achievement updatedAchievement = achievementRepository.save(existingAchievement);
            logger.info("Achievement updated successfully with ID: {}", achievementId);
            return updatedAchievement;
        } catch (Exception e) {
            logger.error("Error while updating achievement with ID: {}", achievementId, e);
            throw e;
        }
    }

    //Delete achievement using the achievementId
    public void deleteAchievement(Integer achievementId){
        logger.info("Deleting achievement with ID: {}", achievementId);
        try {
            Achievement achievement = achievementRepository.findById(achievementId)
                    .orElseThrow(()-> new RuntimeException("Achievement not found to delete!!"));

            achievementRepository.delete(achievement);
            logger.info("Achievement deleted successfully with ID: {}", achievementId);
        } catch (Exception e) {
            logger.error("Error while deleting achievement with ID: {}", achievementId, e);
            throw e;
        }
    }
}
