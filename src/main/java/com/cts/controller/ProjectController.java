package com.cts.controller;


import com.cts.entity.Project;
import com.cts.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@AllArgsConstructor
@RequestMapping("/project")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private ProjectService projectService;

    @PostMapping("/{candidateId}")
    public ResponseEntity<?> addProject(@RequestBody Project candidateProject, @PathVariable Integer candidateId){
        logger.info("Received request to add project for candidateId: {}, Project: {}", candidateId, candidateProject.getProjectName());
        try {
            Project project = projectService.addProject(candidateProject, candidateId);
            logger.info("Successfully added project with ID: {} for candidateId: {}", project.getProjectId(), candidateId);
            return new ResponseEntity<>(project, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error occurred while adding project for candidateId: {}", candidateId, e);
            return ResponseEntity.internalServerError().body("Error adding project: " + e.getMessage());
        }
    }

    @GetMapping("/{projectId}")
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

    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@RequestBody Project candidateProject, @PathVariable Integer projectId){
        logger.info("Received request to update project with ID: {}", projectId);
        try {
            Project project = projectService.updateProject(candidateProject, projectId);
            logger.info("Successfully updated project with ID: {}", projectId);
            return new ResponseEntity<>(project, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while updating project with ID: {}", projectId, e);
            return ResponseEntity.internalServerError().body("Error updating project: " + e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
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
}
