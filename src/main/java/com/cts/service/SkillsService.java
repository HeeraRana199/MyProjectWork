package com.cts.service;

import com.cts.entity.Candidate;
import com.cts.entity.Skills;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.repository.CandidateRepository;
import com.cts.repository.SkillsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@AllArgsConstructor
public class SkillsService {

    private static final Logger logger = LoggerFactory.getLogger(SkillsService.class);
    private SkillsRepository skillsRepository;
    private CandidateRepository candidateRepository;

    public Skills updateSkills(Skills skills, Integer candidateId) {
        logger.info("Updating skills for candidateId: {}", candidateId);
        try {
            Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));

            Skills existingSkills;
            if (candidate.getSkills() == null) {
                logger.debug("No existing skills found for candidateId: {}, creating new skills", candidateId);
                existingSkills = new Skills();
            } else {
                existingSkills = candidate.getSkills();
            }

            if(skills.getProgrammings()!=null){
                logger.debug("Updating programming skills for candidateId: {}", candidateId);
                if(existingSkills.getProgrammings()!=null)
                    existingSkills.setProgrammings(existingSkills.getProgrammings()+","+skills.getProgrammings());
                else
                    existingSkills.setProgrammings(skills.getProgrammings());
            }

            if(skills.getTools()!=null){
                logger.debug("Updating tools skills for candidateId: {}", candidateId);
                if(existingSkills.getTools()!=null)
                    existingSkills.setTools(existingSkills.getTools()+","+skills.getTools());
                else
                    existingSkills.setTools(skills.getTools());
            }

            if(skills.getFrameworks()!=null){
                logger.debug("Updating frameworks skills for candidateId: {}", candidateId);
                if(existingSkills.getFrameworks()!=null)
                    existingSkills.setFrameworks(existingSkills.getFrameworks()+","+skills.getFrameworks());
                else
                    existingSkills.setFrameworks(skills.getFrameworks());
            }

            candidate.setSkills(existingSkills);
            existingSkills.setCandidate(candidate);
            candidate = candidateRepository.save(candidate);
            logger.info("Skills updated successfully for candidateId: {}", candidateId);
            return candidate.getSkills();
        } catch (Exception e) {
            logger.error("Error while updating skills for candidateId: {}", candidateId, e);
            throw e;
        }
    }
}
