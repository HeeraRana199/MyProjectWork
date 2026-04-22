package com.cts.controller;

import com.cts.entity.Achievement;
import com.cts.entity.Certification;
import com.cts.entity.Project;
import com.cts.entity.Skills;
import com.cts.model.ApiResponse;
import com.cts.service.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/trainee")
public class TraineeController {
    private static final Logger logger = LoggerFactory.getLogger(TraineeController.class);
    private CandidateService candidateService;
    private CertificationService certificationService;
    private ProjectService projectService;
    private SkillsService skillsService;
    private AchievementService achievementService;

    @GetMapping("/candidate")
    public ResponseEntity<?> getCandidateById(@RequestParam int id) {
        logger.info("Received request to fetch candidate with ID: {}", id);
        try {
            var candidateDto = candidateService.getCandidateById(id);
            logger.debug("Successfully fetched candidate with ID: {}", id);
            return new ResponseEntity<>(candidateDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching candidate with ID: {}", id, e);
            return ResponseEntity.internalServerError().body("Error fetching candidate: " + e.getMessage());
        }
    }

    @PostMapping("/certificate/{candidateId}")
    public ResponseEntity<?> registerCertification(@RequestBody Certification certification, @PathVariable Integer candidateId){
        logger.info("Received request to register certification for candidateId: {}", candidateId);
        try {
            certification = certificationService.registerCertification(certification, candidateId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(201);
            apiResponse.setMessage("Certificate is added");
            logger.info("Successfully registered certification for candidateId: {}", candidateId);
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error occurred while registering certification for candidateId: {}", candidateId, e);
            return ResponseEntity.internalServerError().body("Error registering certification: " + e.getMessage());
        }
    }
    @GetMapping("/certificate/{certificationId}")
    public ResponseEntity<?> getCertification(@PathVariable String certificationId){
        logger.info("Received request to fetch certification with ID: {}", certificationId);
        try {
            Certification certification = certificationService.getCertification(certificationId);
            logger.debug("Successfully fetched certification with ID: {}", certificationId);
            return ResponseEntity.ok(certification);
        } catch (Exception e) {
            logger.error("Error occurred while fetching certification with ID: {}", certificationId, e);
            return ResponseEntity.internalServerError().body("Error fetching certification: " + e.getMessage());
        }
    }

    //Update Certification
    @PatchMapping("/certificate/{certificationId}")
    public ResponseEntity<?> updateCertification(@RequestBody Certification certification, @PathVariable String certificationId){
        logger.info("Received request to update certification with ID: {}", certificationId);
        try {
            Certification updatedCertification = certificationService.updateCertification(certification, certificationId);
            logger.info("Successfully updated certification with ID: {}", certificationId);
            return ResponseEntity.ok(updatedCertification);
        } catch (Exception e) {
            logger.error("Error occurred while updating certification with ID: {}", certificationId, e);
            return ResponseEntity.internalServerError().body("Error updating certification: " + e.getMessage());
        }
    }

    //Delete Certification
    @DeleteMapping("/certificate/{certificationId}")
    public ResponseEntity<?> deleteCertification(@PathVariable String certificationId){
        logger.info("Received request to delete certification with ID: {}", certificationId);
        try {
            certificationService.deleteCertification(certificationId);
            logger.info("Successfully deleted certification with ID: {}", certificationId);
            return ResponseEntity.ok("Certificate deleted successfully!!");
        } catch (Exception e) {
            logger.error("Error occurred while deleting certification with ID: {}", certificationId, e);
            return ResponseEntity.internalServerError().body("Error deleting certification: " + e.getMessage());
        }
    }



    @PostMapping("/project/{candidateId}")
    public ResponseEntity<?> addProject(@RequestBody Project candidateProject, @PathVariable Integer candidateId){
        logger.info("Received request to add project for candidateId: {}, Project: {}", candidateId, candidateProject.getProjectName());
        try {
            Project project = projectService.addProject(candidateProject, candidateId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(201);
            apiResponse.setMessage("Project is added");
            logger.info("Successfully added project with ID: {} for candidateId: {}", project.getProjectId(), candidateId);
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error occurred while adding project for candidateId: {}", candidateId, e);
            return ResponseEntity.internalServerError().body("Error adding project: " + e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable Integer projectId){
        logger.info("Received request to fetch project with ID: {}", projectId);
        try {
            Project project = projectService.getProject(projectId);
            logger.debug("Successfully fetched project with ID: {}", projectId);
            return new ResponseEntity<>(project, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching project with ID: {}", projectId, e);
            return ResponseEntity.internalServerError().body("Error fetching project: " + e.getMessage());
        }
    }

    @PutMapping("/project/{projectId}")
    public ResponseEntity<?> updateProject(@RequestBody Project candidateProject, @PathVariable Integer projectId){
        logger.info("Received request to update project with ID: {}", projectId);
        try {
            Project project = projectService.updateProject(candidateProject, projectId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(200);
            apiResponse.setMessage("Project is updated");
            logger.info("Successfully updated project with ID: {}", projectId);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while updating project with ID: {}", projectId, e);
            return ResponseEntity.internalServerError().body("Error updating project: " + e.getMessage());
        }
    }

    @DeleteMapping("/project/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer projectId){
        logger.info("Received request to delete project with ID: {}", projectId);
        try {
            projectService.deleteProject(projectId);
            logger.info("Successfully deleted project with ID: {}", projectId);
            return new ResponseEntity<>("Project deleted successfully!!", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while deleting project with ID: {}", projectId, e);
            return ResponseEntity.internalServerError().body("Error deleting project: " + e.getMessage());
        }
    }




    @PutMapping("/skill/{candidateId}")
    public ResponseEntity<?> updateSkills(@PathVariable Integer candidateId, @RequestBody Skills skills){
        logger.info("Received request to update skills for candidateId: {}", candidateId);
        try {
            skills = skillsService.updateSkills(skills, candidateId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(200);
            apiResponse.setMessage("Skills are updated");
            logger.info("Successfully updated skills for candidateId: {}", candidateId);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while updating skills for candidateId: {}", candidateId, e);
            return ResponseEntity.internalServerError().body("Error updating skills: " + e.getMessage());
        }
    }




    @PostMapping("/achievement/{candidateId}")
    public ResponseEntity<?> addAchievement(@RequestBody Achievement candidateAchievement, @PathVariable Integer candidateId){
        logger.info("Received request to add achievement for candidateId: {}", candidateId);
        try {
            Achievement achievement = achievementService.addAchievement(candidateAchievement, candidateId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(201);
            apiResponse.setMessage("Achievement is added");
            logger.info("Successfully added achievement with ID: {} for candidateId: {}", achievement.getAId(), candidateId);
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error occurred while adding achievement for candidateId: {}", candidateId, e);
            return ResponseEntity.internalServerError().body("Error adding achievement: " + e.getMessage());
        }
    }

    @GetMapping("/achievement/{achievementId}")
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

    @PutMapping("/achievement/{achievementId}")
    public ResponseEntity<?> updateAchievement(@RequestBody Achievement candidateAchievement, @PathVariable Integer achievementId){
        logger.info("Received request to update achievement with ID: {}", achievementId);
        try {
            Achievement achievement = achievementService.updateAchievement(candidateAchievement, achievementId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(200);
            apiResponse.setMessage("Achievement is updated");
            logger.info("Successfully updated achievement with ID: {}", achievementId);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while updating achievement with ID: {}", achievementId, e);
            return ResponseEntity.internalServerError().body("Error updating achievement: " + e.getMessage());
        }
    }

    @DeleteMapping("/achievement/{achievementId}")
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
