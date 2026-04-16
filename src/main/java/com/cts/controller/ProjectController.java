package com.cts.controller;


import com.cts.entity.Project;
import com.cts.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/project")
public class ProjectController {

    private ProjectService projectService;

    @PostMapping("/{candidateId}")
    public ResponseEntity<?> addProject(@RequestBody Project candidateProject, @PathVariable Integer candidateId){
        Project project = projectService.addProject(candidateProject,candidateId);
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable Integer projectId){
        Project project = projectService.getProject(projectId);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@RequestBody Project candidateProject, @PathVariable Integer projectId){
        Project project = projectService.updateProject(candidateProject, projectId);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer projectId){
        projectService.deleteProject(projectId);
        return new ResponseEntity<>("Project deleted successfully!!", HttpStatus.OK);
    }
}
