package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Project;
import com.cts.repository.CandidateRepository;
import com.cts.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProjectService {

    private ProjectRepository projectRepository;
    private CandidateRepository candidateRepository;

    //create project logic
    public Project addProject(Project project, Integer candidateId){
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(()-> new RuntimeException("candidate not found for creating the project!!"));

        project.setCandidate(candidate);
        return projectRepository.save(project);
    }

    //fetch project logic
    public Project getProject(Integer projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new RuntimeException("Project not found with the this projectId!!"));

        return project;
    }

    //update project logic
    public Project updateProject(Project project, Integer projectId){
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(()-> new RuntimeException("Project not found with the this projectId!!"));

        if(project.getProjectName() != null){
            existingProject.setProjectName(project.getProjectName());
        }
        if(project.getTech()!=null){
            existingProject.setTech(project.getTech());
        }
        if(project.getOutcome()!=null){
            existingProject.setOutcome(project.getOutcome());
        }
        if(project.getRole()!=null){
            existingProject.setRole(project.getRole());
        }

        return projectRepository.save(existingProject);
    }

    //delete project logic
    public void deleteProject(Integer projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new RuntimeException("Project not found with the this projectId!!"));

        projectRepository.delete(project);
    }
}
