//package com.cts.controller;
//
//import com.cts.entity.Skills;
//import com.cts.service.SkillsService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@RestController
//@AllArgsConstructor
//@RequestMapping("/skills")
//public class SkillController {
//
//    private static final Logger logger = LoggerFactory.getLogger(SkillController.class);
//    private SkillsService skillsService;
//
//    @PutMapping("/update/{candidateId}")
//    public ResponseEntity<?> updateSkills(@PathVariable Integer candidateId, @RequestBody Skills skills){
//        logger.info("Received request to update skills for candidateId: {}", candidateId);
//        try {
//            skills = skillsService.updateSkills(skills, candidateId);
//            logger.info("Successfully updated skills for candidateId: {}", candidateId);
//            return new ResponseEntity<>(skills, HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error occurred while updating skills for candidateId: {}", candidateId, e);
//            return ResponseEntity.internalServerError().body("Error updating skills: " + e.getMessage());
//        }
//    }
//}
