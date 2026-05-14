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

    public Skills updateSkills(Skills skills, Integer associateId) {
        logger.info("Updating skills for associateId: {}", associateId);
        try {
            Candidate candidate = candidateRepository.findById(associateId)
                    .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));

            Skills existingSkills;
            if (candidate.getSkills() == null) {
                logger.debug("No existing skills found for associateId: {}, creating new skills", associateId);
                existingSkills = new Skills();
            } else {
                existingSkills = candidate.getSkills();
            }

            if(skills.getProgrammings()!=null){
                logger.debug("Updating programming skills for associateId: {}", associateId);
//                if(existingSkills.getProgrammings()!=null)
//                    existingSkills.setProgrammings(existingSkills.getProgrammings()+","+skills.getProgrammings());
//                else
                    existingSkills.setProgrammings(skills.getProgrammings());
            }

            if(skills.getTools()!=null){
                logger.debug("Updating tools skills for associateId: {}", associateId);
//                if(existingSkills.getTools()!=null)
//                    existingSkills.setTools(existingSkills.getTools()+","+skills.getTools());
//                else
                    existingSkills.setTools(skills.getTools());
            }

            if(skills.getFrameworks()!=null){
                logger.debug("Updating frameworks skills for associateId: {}", associateId);
//                if(existingSkills.getFrameworks()!=null)
//                    existingSkills.setFrameworks(existingSkills.getFrameworks()+","+skills.getFrameworks());
//                else
                    existingSkills.setFrameworks(skills.getFrameworks());
            }

            candidate.setSkills(existingSkills);
            existingSkills.setCandidate(candidate);
            candidate = candidateRepository.save(candidate);
            logger.info("Skills updated successfully for associateId: {}", associateId);
            return candidate.getSkills();
        } catch (Exception e) {
            logger.error("Error while updating skills for associateId: {}", associateId, e);
            throw e;
        }
    }
}
