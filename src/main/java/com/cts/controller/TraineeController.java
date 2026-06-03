package com.cts.controller;

import com.cts.entity.Achievement;
import com.cts.entity.Certification;
import com.cts.entity.Project;
import com.cts.entity.Skills;
import com.cts.model.ApiResponse;
import com.cts.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@RestController
@RequiredArgsConstructor
@RequestMapping("/trainee")
@Tag(name = "Trainee", description = "Trainee self-service: own talent card, certifications, projects, skills, achievements, profile photo. Requires ROLE_TRAINEE.")
public class TraineeController {

    private static final Logger logger =
            LoggerFactory.getLogger(TraineeController.class);

    //  Final → constructor injection works
    private final CandidateService candidateService;
    private final CertificationService certificationService;
    private final ProjectService projectService;
    private final SkillsService skillsService;
    private final AchievementService achievementService;

    // @Value must stay as FIELD injection
    @Value("${file.upload-dir}")
    private String uploadDir;


    @GetMapping("/candidate")
    @Operation(summary = "Get own talent card",
            description = "Returns the trainee's full profile (skills, projects, certifications, achievements, scores).")
    public ResponseEntity<?> getAssociateById(@RequestParam int id) {
        logger.info("Received request to fetch candidate with ID: {}", id);
        try {
            var candidateDto = candidateService.getAssociateById(id);
            logger.debug("Successfully fetched candidate with ID: {}", id);
            return new ResponseEntity<>(candidateDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching candidate with ID: {}", id, e);
            return ResponseEntity.internalServerError().body("Error fetching candidate: " + e.getMessage());
        }
    }

    @PostMapping("/certificate/{associateId}")
    @Operation(summary = "Register a new certification",
            description = "Links the certification to the trainee. Status defaults to false (pending verification).")
    public ResponseEntity<?> registerCertification(@RequestBody @Valid Certification certification, @PathVariable Integer associateId){
        logger.info("Received request to register certification for associateId: {}", associateId);
        try {
            certification = certificationService.registerCertification(certification, associateId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(201);
            apiResponse.setMessage("Certificate is added");
            logger.info("Successfully registered certification for associateId: {}", associateId);
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error occurred while registering certification for associateId: {}", associateId, e);
            return ResponseEntity.internalServerError().body("Error registering certification: " + e.getMessage());
        }
    }
    @GetMapping("/certificate/{certificationId}")
    @Operation(summary = "Get a certification by id")
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
    @Operation(summary = "Patch a certification (partial update)",
            description = "Only non-null fields on the request body are applied to the existing entity.")
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
    @Operation(summary = "Delete a certification")
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



    @PostMapping("/project/{associateId}")
    @Operation(summary = "Add a project to a trainee")
    public ResponseEntity<?> addProject(@RequestBody @Valid Project candidateProject, @PathVariable Integer associateId){
        logger.info("Received request to add project for associateId: {}, Project: {}", associateId, candidateProject.getProjectName());
        try {
            Project project = projectService.addProject(candidateProject, associateId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(201);
            apiResponse.setMessage("Project is added");
            logger.info("Successfully added project with ID: {} for associateId: {}", project.getProjectId(), associateId);
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error occurred while adding project for associateId: {}", associateId, e);
            return ResponseEntity.internalServerError().body("Error adding project: " + e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get a project by id")
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
    @Operation(summary = "Update a project (partial)",
            description = "Only non-null fields on the request body are applied.")
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
    @Operation(summary = "Delete a project")
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




    @PutMapping("/skill/{associateId}")
    @Operation(summary = "Update trainee skills",
            description = "Creates a Skills row if the trainee has none, otherwise patches non-null fields onto the existing row.")
    public ResponseEntity<?> updateSkills(@PathVariable Integer associateId, @RequestBody Skills skills){
        logger.info("Received request to update skills for associateId: {}", associateId);
        try {
            skills = skillsService.updateSkills(skills, associateId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(200);
            apiResponse.setMessage("Skills are updated");
            logger.info("Successfully updated skills for associateId: {}", associateId);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while updating skills for associateId: {}", associateId, e);
            return ResponseEntity.internalServerError().body("Error updating skills: " + e.getMessage());
        }
    }




    @PostMapping("/achievement/{associateId}")
    @Operation(summary = "Add an achievement")
    public ResponseEntity<?> addAchievement(@RequestBody @Valid Achievement candidateAchievement, @PathVariable Integer associateId){
        logger.info("Received request to add achievement for associateId: {}", associateId);
        try {
            Achievement achievement = achievementService.addAchievement(candidateAchievement, associateId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(201);
            apiResponse.setMessage("Achievement is added");
            logger.info("Successfully added achievement with ID: {} for associateId: {}", achievement.getAId(), associateId);
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error occurred while adding achievement for associateId: {}", associateId, e);
            return ResponseEntity.internalServerError().body("Error adding achievement: " + e.getMessage());
        }
    }

    @GetMapping("/achievement/{achievementId}")
    @Operation(summary = "Get an achievement by id")
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
    @Operation(summary = "Update an achievement (partial)")
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
    @Operation(summary = "Delete an achievement")
    public ResponseEntity<?> deleteAchievement(@PathVariable Integer achievementId){
        logger.info("Received request to delete achievement with ID: {}", achievementId);
        try {
            achievementService.deleteAchievement(achievementId);
            logger.info("Successfully deleted achievement with ID: {}", achievementId);
            return new ResponseEntity<>("Achievement deleted successfully!!", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while deleting achievement with ID: {}", achievementId, e);
            return ResponseEntity.internalServerError().body("Error deleting achievement: " + e.getMessage());
        }
    }

    //Upload Trainee Profile Photo
    // ✅ Upload / overwrite image
    @PostMapping("/profile-photo/{associateId}")
    @Operation(summary = "Upload / overwrite a profile photo",
            description = "Accepts a multipart file under the form key `file`. Stores it as `{associateId}.jpg` under the configured upload directory.")
    public ResponseEntity<String> uploadProfileImage(
            @PathVariable Long associateId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty file");
        }

        Files.createDirectories(Paths.get(uploadDir));

        String filename = associateId + ".jpg";
        Path filePath = Paths.get(uploadDir).resolve(filename);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return ResponseEntity.ok("Uploaded successfully");
    }

    // ✅ Fetch image by associateId
    @GetMapping("/profile-photo/{associateId}")
    @Operation(summary = "Fetch a profile photo",
            description = "Returns the JPG stored under `{associateId}.jpg`. Falls back to `000000.jpg` if no specific photo exists.")
    public ResponseEntity<Resource> getProfileImage(
            @PathVariable Long associateId
    ) throws MalformedURLException {

        Path imagePath = Paths.get(uploadDir).resolve(associateId + ".jpg");

        if (!Files.exists(imagePath)) {

            //return ResponseEntity.notFound().build();
            Path defaultPath = Paths.get(uploadDir).resolve("000000.jpg");
            Resource resource = new UrlResource(defaultPath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        }

        Resource resource = new UrlResource(imagePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}
