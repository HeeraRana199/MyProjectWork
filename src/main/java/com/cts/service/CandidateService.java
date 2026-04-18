package com.cts.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cts.dto.CandidateDto;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.mapper.CandidateRowMapper;
import com.cts.util.CandidateExcelHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cts.entity.Candidate;
import com.cts.repository.CandidateRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CandidateService {

    private static final Logger logger = LoggerFactory.getLogger(CandidateService.class);
    private CandidateRepository candidateRepository;
    private CandidateRowMapper candidateRowMapper;

    public static class ExcelUploadResult {
        private int totalRecords;
        private int savedRecords;
        private int rejectedRecords;
        private int mergedRecords;
        private List<String> errors;
        private String schemaValidationMessage;

        public ExcelUploadResult() {
            this.errors = new ArrayList<>();
        }

        // Getters and setters
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        public int getSavedRecords() { return savedRecords; }
        public void setSavedRecords(int savedRecords) { this.savedRecords = savedRecords; }
        public int getRejectedRecords() { return rejectedRecords; }
        public void setRejectedRecords(int rejectedRecords) { this.rejectedRecords = rejectedRecords; }
        public int getMergedRecords() { return mergedRecords; }
        public void setMergedRecords(int mergedRecords) { this.mergedRecords = mergedRecords; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public String getSchemaValidationMessage() { return schemaValidationMessage; }
        public void setSchemaValidationMessage(String schemaValidationMessage) { this.schemaValidationMessage = schemaValidationMessage; }
    }

    //Create candidate logic
    public Candidate addCandidate(Candidate candidate) {
        logger.info("Adding candidate with ID: {}, Name: {}", candidate.getCognizantCandidateId(), candidate.getCandidateName());
        try {
            Candidate savedCandidate = candidateRepository.save(candidate);
            logger.debug("Candidate saved successfully with ID: {}", savedCandidate.getCognizantCandidateId());
            return savedCandidate;
        } catch (Exception e) {
            logger.error("Error while saving candidate: {}", candidate.getCognizantCandidateId(), e);
            throw e;
        }
    }

    //Get candidate info logic
    @Transactional
    public CandidateDto getCandidateById(int candidateId) {
        logger.debug("Fetching candidate with ID: {}", candidateId);
        try {
            Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));

            CandidateDto candidateDto = candidateRowMapper.convertToCandidateDto(candidate);
            logger.debug("Successfully retrieved candidate with ID: {}", candidateId);
            return candidateDto;
        } catch (CandidateNotFoundException e) {
            logger.warn("Candidate not found with ID: {}", candidateId);
            throw e;
        } catch (Exception e) {
            logger.error("Error while fetching candidate with ID: {}", candidateId, e);
            throw e;
        }
    }

    //Excel Upload Logic with validation, batch processing, and duplicate handling
    @Transactional
    public ExcelUploadResult saveCandidatesFromExcel(MultipartFile file) {
        ExcelUploadResult result = new ExcelUploadResult();
        logger.info("Starting Excel upload process for file: {}", file.getOriginalFilename());

        try {
            InputStream is = file.getInputStream();

            // Step 1: Validate schema
            logger.debug("Validating Excel schema for file: {}", file.getOriginalFilename());
            CandidateExcelHelper.ValidationResult validation = CandidateExcelHelper.validateExcelSchema(is);
            result.setSchemaValidationMessage(validation.getMessage());

            if (!validation.isValid()) {
                logger.warn("Schema validation failed for file: {}. Message: {}", file.getOriginalFilename(), validation.getMessage());
                result.getErrors().add(validation.getMessage());
                return result;
            }

            logger.debug("Schema validation passed for file: {}", file.getOriginalFilename());

            // Reset input stream for parsing
            is = file.getInputStream();

            // Step 2: Parse candidates
            logger.debug("Parsing candidates from Excel file: {}", file.getOriginalFilename());
            List<Candidate> candidates = CandidateExcelHelper.excelToCandidates(is);
            result.setTotalRecords(candidates.size());
            logger.info("Parsed {} candidates from file: {}", candidates.size(), file.getOriginalFilename());

            // Step 3: Process in batches of 50
            final int BATCH_SIZE = 50;
            List<Candidate> toSave = new ArrayList<>();
            List<Candidate> toUpdate = new ArrayList<>();

            logger.debug("Starting batch processing of candidates");
            for (int i = 0; i < candidates.size(); i++) {
                Candidate candidate = candidates.get(i);

                // Check for duplicates
                Optional<Candidate> existing = candidateRepository.findById(candidate.getCognizantCandidateId());

                if (existing.isPresent()) {
                    // Check if data has changed
                    if (hasChanges(existing.get(), candidate)) {
                        // Merge changes
                        logger.debug("Merging changes for existing candidate ID: {}", candidate.getCognizantCandidateId());
                        Candidate merged = mergeCandidates(existing.get(), candidate);
                        toUpdate.add(merged);
                        result.setMergedRecords(result.getMergedRecords() + 1);
                    } else {
                        // Exact duplicate, reject
                        logger.debug("Rejecting duplicate candidate ID: {}", candidate.getCognizantCandidateId());
                        result.setRejectedRecords(result.getRejectedRecords() + 1);
                    }
                } else {
                    // New candidate
                    logger.debug("Adding new candidate ID: {}", candidate.getCognizantCandidateId());
                    toSave.add(candidate);
                }

                // Process batch when it reaches BATCH_SIZE or at the end
                if (toSave.size() >= BATCH_SIZE || i == candidates.size() - 1) {
                    if (!toSave.isEmpty()) {
                        logger.debug("Saving batch of {} candidates", toSave.size());
                        candidateRepository.saveAll(toSave);
                        result.setSavedRecords(result.getSavedRecords() + toSave.size());
                        toSave.clear();
                    }
                }

                // Process updates in batches too
                if (toUpdate.size() >= BATCH_SIZE || i == candidates.size() - 1) {
                    if (!toUpdate.isEmpty()) {
                        logger.debug("Updating batch of {} candidates", toUpdate.size());
                        candidateRepository.saveAll(toUpdate);
                        toUpdate.clear();
                    }
                }
            }

            logger.info("Excel upload completed successfully. Total: {}, Saved: {}, Updated: {}, Rejected: {}",
                    result.getTotalRecords(), result.getSavedRecords(), result.getMergedRecords(), result.getRejectedRecords());

        } catch (Exception e) {
            logger.error("Failed to process Excel file: {}", file.getOriginalFilename(), e);
            result.getErrors().add("Failed to process Excel file: " + e.getMessage());
        }

        return result;
    }

    private boolean hasChanges(Candidate existing, Candidate newCandidate) {
        return !equals(existing.getAssociateId(), newCandidate.getAssociateId()) ||
               !equals(existing.getCandidateName(), newCandidate.getCandidateName()) ||
               !equals(existing.getCognizantEmailID(), newCandidate.getCognizantEmailID()) ||
               !equals(existing.getGender(), newCandidate.getGender()) ||
               !equals(existing.getCohortCode(), newCandidate.getCohortCode()) ||
               !equals(existing.getDeploymentLocation(), newCandidate.getDeploymentLocation()) ||
               !equals(existing.getTrackName(), newCandidate.getTrackName());
    }

    private Candidate mergeCandidates(Candidate existing, Candidate newCandidate) {
        // Update existing with new non-null values
        if (newCandidate.getAssociateId() != null) {
            existing.setAssociateId(newCandidate.getAssociateId());
        }
        if (newCandidate.getCandidateName() != null && !newCandidate.getCandidateName().isEmpty()) {
            existing.setCandidateName(newCandidate.getCandidateName());
        }
        if (newCandidate.getCognizantEmailID() != null && !newCandidate.getCognizantEmailID().isEmpty()) {
            existing.setCognizantEmailID(newCandidate.getCognizantEmailID());
        }
        if (newCandidate.getGender() != null && !newCandidate.getGender().isEmpty()) {
            existing.setGender(newCandidate.getGender());
        }
        if (newCandidate.getCohortCode() != null && !newCandidate.getCohortCode().isEmpty()) {
            existing.setCohortCode(newCandidate.getCohortCode());
        }
        if (newCandidate.getDeploymentLocation() != null && !newCandidate.getDeploymentLocation().isEmpty()) {
            existing.setDeploymentLocation(newCandidate.getDeploymentLocation());
        }
        if (newCandidate.getTrackName() != null && !newCandidate.getTrackName().isEmpty()) {
            existing.setTrackName(newCandidate.getTrackName());
        }
        return existing;
    }

    private boolean equals(Object obj1, Object obj2) {
        return (obj1 == null && obj2 == null) || (obj1 != null && obj1.equals(obj2));
    }
}