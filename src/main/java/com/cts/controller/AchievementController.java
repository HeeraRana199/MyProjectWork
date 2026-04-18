package com.cts.controller;

import com.cts.entity.Achievement;
import com.cts.service.AchievementService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@AllArgsConstructor
@RequestMapping("/achievement")
public class AchievementController {

    private static final Logger logger = LoggerFactory.getLogger(AchievementController.class);
    private AchievementService achievementService;

    @PostMapping("/{candidateId}")
    public ResponseEntity<?> addAchievement(@RequestBody Achievement candidateAchievement, @PathVariable Integer candidateId){
        logger.info("Received request to add achievement for candidateId: {}", candidateId);
        try {
            Achievement achievement = achievementService.addAchievement(candidateAchievement, candidateId);
            logger.info("Successfully added achievement with ID: {} for candidateId: {}", achievement.getAId(), candidateId);
            return new ResponseEntity<>(achievement, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error occurred while adding achievement for candidateId: {}", candidateId, e);
            return ResponseEntity.internalServerError().body("Error adding achievement: " + e.getMessage());
        }
    }

    @GetMapping("/{achievementId}")
    public ResponseEntity<?> getAchievement(@PathVariable Integer achievementId){
        logger.info("Received request to fetch achievement with ID: {}", achievementId);
        try {
            Achievement achievement = achievementService.getAchievement(achievementId);
            logger.debug("Successfully fetched achievement with ID: {}", achievementId);
            return new ResponseEntity<>(achievement, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching achievement with ID: {}", achievementId, e);
            return ResponseEntity.internalServerError().body("Error fetching achievement: " + e.getMessage());
        }
    }

    @PutMapping("/{achievementId}")
    public ResponseEntity<?> updateAchievement(@RequestBody Achievement candidateAchievement, @PathVariable Integer achievementId){
        logger.info("Received request to update achievement with ID: {}", achievementId);
        try {
            Achievement achievement = achievementService.updateAchievement(candidateAchievement, achievementId);
            logger.info("Successfully updated achievement with ID: {}", achievementId);
            return new ResponseEntity<>(achievement, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while updating achievement with ID: {}", achievementId, e);
            return ResponseEntity.internalServerError().body("Error updating achievement: " + e.getMessage());
        }
    }

    @DeleteMapping("/{achievementId}")
    public ResponseEntity<?> deleteAchievement(@PathVariable Integer candidateId){
        logger.info("Received request to delete achievement with ID: {}", candidateId);
        try {
            achievementService.deleteAchievement(candidateId);
            logger.info("Successfully deleted achievement with ID: {}", candidateId);
            return new ResponseEntity<>("Achievement deleted successfully!!", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while deleting achievement with ID: {}", candidateId, e);
            return ResponseEntity.internalServerError().body("Error deleting achievement: " + e.getMessage());
        }
    }
}
