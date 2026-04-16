package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Skills;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.repository.CandidateRepository;
import com.cts.repository.SkillsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SkillsService {

    private SkillsRepository skillsRepository;
    private CandidateRepository candidateRepository;

    public Skills updateSkills(Skills skills, Integer candidateId) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));

        Skills existingSkills;
        if (candidate.getSkills() == null)
            existingSkills = new Skills();
        else
            existingSkills = candidate.getSkills();

        if(skills.getProgrammings()!=null){
            if(existingSkills.getProgrammings()!=null)
                existingSkills.setProgrammings(existingSkills.getProgrammings()+","+skills.getProgrammings());
            else
                existingSkills.setProgrammings(skills.getProgrammings());
        }

        if(skills.getTools()!=null){
            if(existingSkills.getTools()!=null)
                existingSkills.setTools(existingSkills.getTools()+","+skills.getTools());
            else
                existingSkills.setTools(skills.getTools());

        }

        if(skills.getFrameworks()!=null){
            if(existingSkills.getFrameworks()!=null)
                existingSkills.setFrameworks(existingSkills.getFrameworks()+","+skills.getFrameworks());
            else
                existingSkills.setFrameworks(skills.getFrameworks());

        }

        candidate.setSkills(existingSkills);
        existingSkills.setCandidate(candidate);
        candidate = candidateRepository.save(candidate);
        return candidate.getSkills();
    }
}
