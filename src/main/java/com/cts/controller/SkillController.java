package com.cts.controller;

import com.cts.entity.Skills;
import com.cts.service.SkillsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/skills")
public class SkillController {

    private SkillsService skillsService;

    @PutMapping("/update/{candidateId}")
    public ResponseEntity<?> updateSkills(@PathVariable Integer candidateId, @RequestBody Skills skills){
        skills=skillsService.updateSkills(skills,candidateId);
        return new ResponseEntity<>(skills , HttpStatus.OK);
    }
}

