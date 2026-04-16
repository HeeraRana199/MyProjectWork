package com.cts.controller;

import com.cts.entity.Achievement;
import com.cts.service.AchievementService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/achievement")
public class AchievementController {

    private AchievementService achievementService;

    @PostMapping("/{candidateId}")
    public ResponseEntity<?> addAchievement(@RequestBody Achievement candidateAchievement, @PathVariable Integer candidateId){
        Achievement achievement = achievementService.addAchievement(candidateAchievement, candidateId);
        return new ResponseEntity<>(achievement, HttpStatus.CREATED);
    }

    @GetMapping("/{achievementId}")
    public ResponseEntity<?> getAchievement(@PathVariable Integer achievementId){
        Achievement achievement = achievementService.getAchievement(achievementId);
        return new ResponseEntity<>(achievement, HttpStatus.OK);
    }

    @PutMapping("/{achievementId}")
    public ResponseEntity<?> updateAchievement(@RequestBody Achievement candidateAchievement, @PathVariable Integer achievementId){
        Achievement achievement = achievementService.updateAchievement(candidateAchievement, achievementId);
        return new ResponseEntity<>(achievement, HttpStatus.OK);
    }

    @DeleteMapping("/{achievementId}")
    public ResponseEntity<?> deleteAchievement(@PathVariable Integer candidateId){
        achievementService.deleteAchievement(candidateId);
        return new ResponseEntity<>("Achievement deleted successfully!!", HttpStatus.OK);
    }
}
