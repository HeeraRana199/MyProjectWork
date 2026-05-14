package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Project;
import com.cts.repository.CandidateRepository;
import com.cts.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@AllArgsConstructor
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private ProjectRepository projectRepository;
    private CandidateRepository candidateRepository;

    //create project logic
    public Project addProject(Project project, Integer associateId){
        logger.info("Adding project for associateId: {}, Project: {}", associateId, project.getProjectName());
        try {
            Candidate candidate = candidateRepository.findById(associateId)
                    .orElseThrow(()-> new RuntimeException("candidate not found for creating the project!!"));

            project.setCandidate(candidate);
            Project savedProject = projectRepository.save(project);
            logger.debug("Project saved successfully with ID: {} for associateId: {}", savedProject.getProjectId(), associateId);
            return savedProject;
        } catch (Exception e) {
            logger.error("Error while adding project for associateId: {}", associateId, e);
            throw e;
        }
    }

    //fetch project logic
    public Project getProject(Integer projectId){
        logger.debug("Fetching project with ID: {}", projectId);
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(()-> new RuntimeException("Project not found with the this projectId!!"));

            logger.debug("Successfully retrieved project with ID: {}", projectId);
            return project;
        } catch (Exception e) {
            logger.error("Error while fetching project with ID: {}", projectId, e);
            throw e;
        }
    }

    //update project logic
    public Project updateProject(Project project, Integer projectId){
        logger.info("Updating project with ID: {}", projectId);
        try {
            Project existingProject = projectRepository.findById(projectId)
                    .orElseThrow(()-> new RuntimeException("Project not found with the this projectId!!"));

            if(project.getProjectName() != null){
                logger.debug("Updating project name for ID: {} to: {}", projectId, project.getProjectName());
                existingProject.setProjectName(project.getProjectName());
            }
            if(project.getTech()!=null){
                logger.debug("Updating project tech for ID: {}", projectId);
                existingProject.setTech(project.getTech());
            }
            if(project.getOutcome()!=null){
                logger.debug("Updating project outcome for ID: {}", projectId);
                existingProject.setOutcome(project.getOutcome());
            }
            if(project.getRole()!=null){
                logger.debug("Updating project role for ID: {}", projectId);
                existingProject.setRole(project.getRole());
            }

            Project updatedProject = projectRepository.save(existingProject);
            logger.info("Project updated successfully with ID: {}", projectId);
            return updatedProject;
        } catch (Exception e) {
            logger.error("Error while updating project with ID: {}", projectId, e);
            throw e;
        }
    }

    //delete project logic
    public void deleteProject(Integer projectId){
        logger.info("Deleting project with ID: {}", projectId);
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(()-> new RuntimeException("Project not found with the this projectId!!"));

            projectRepository.delete(project);
            logger.info("Project deleted successfully with ID: {}", projectId);
        } catch (Exception e) {
            logger.error("Error while deleting project with ID: {}", projectId, e);
            throw e;
        }
    }
}
