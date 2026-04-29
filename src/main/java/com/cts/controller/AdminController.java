package com.cts.controller;

import com.cts.entity.Candidate;
import com.cts.service.CandidateService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private CandidateService candidateService;

    // ✅ Excel Upload API with comprehensive validation and processing
    @PostMapping(value = "/candidate/upload",consumes = "multipart/form-data")
    public ResponseEntity<?> uploadExcel(@RequestPart MultipartFile file) {
        logger.info("Received Excel upload request with file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        try {
            CandidateService.ExcelUploadResult result = candidateService.saveCandidatesFromExcel(file);

            if (!result.getErrors().isEmpty()) {
                logger.warn("Excel validation failed for file: {}. Errors: {}", file.getOriginalFilename(), result.getErrors());
                return ResponseEntity.badRequest().body(result);
            }

            logger.info("Excel file processed successfully. Total: {}, Saved: {}, Updated: {}, Rejected: {}",
                    result.getTotalRecords(), result.getSavedRecords(), result.getMergedRecords(), result.getRejectedRecords());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error processing Excel file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError()
                    .body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping("/candidate")
    public ResponseEntity<?> getCandidateById(@RequestParam int id) {
        logger.info("Received request to fetch candidate with ID: {}", id);
        try {
            var candidateDto = candidateService.getCandidateById(id);
            logger.debug("Successfully fetched candidate with ID: {}", id);
            return new ResponseEntity<>(candidateDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching candidate with ID: {}", id, e);
            return ResponseEntity.internalServerError().body("Error fetching candidate: " + e.getMessage());
        }
    }


    @GetMapping("/allcandidates")
    public ResponseEntity<?> getAllCandidates() {
        logger.info("Received request to fetch all candidates");
        try {
            var candidates = candidateService.getAllCandidates();
            logger.debug("Successfully fetched {} candidates", candidates.size());
            return new ResponseEntity<>(candidates, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching all candidates", e);
            return ResponseEntity.internalServerError().body("Error fetching candidates: " + e.getMessage());
        }
    }

    @DeleteMapping("/candidate/{candidateId}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Integer candidateId) {
        logger.info("Received request to delete candidate with ID: {}", candidateId);

        try {
            candidateService.deleteCandidateById(candidateId);
            logger.info("Successfully deleted candidate with ID: {}", candidateId);

            return ResponseEntity.ok("Candidate and all associated data deleted successfully");

        } catch (Exception e) {
            logger.error("Error occurred while deleting candidate with ID: {}", candidateId, e);
            return ResponseEntity.internalServerError()
                    .body("Error deleting candidate: " + e.getMessage());
        }
    }
}
