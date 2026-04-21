//package com.cts.controller;
//
//import java.util.List;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.cts.entity.Candidate;
//import com.cts.service.CandidateService;
//
//import lombok.AllArgsConstructor;
//
//@RestController
//@AllArgsConstructor
//public class CandidateController {
//
//    private static final Logger logger = LoggerFactory.getLogger(CandidateController.class);
//    private CandidateService candidateService;
//
//    @PostMapping("/candidate")
//    public ResponseEntity<?> addCandidate(@RequestBody Candidate candidate) {
//        logger.info("Received request to add candidate with ID: {}", candidate.getCognizantCandidateId());
//        try {
//            Candidate savedCandidate = candidateService.addCandidate(candidate);
//            logger.info("Successfully added candidate with ID: {}", savedCandidate.getCognizantCandidateId());
//            return new ResponseEntity<>(savedCandidate, HttpStatus.CREATED);
//        } catch (Exception e) {
//            logger.error("Error occurred while adding candidate: ", e);
//            return ResponseEntity.internalServerError().body("Error adding candidate: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/candidate")
//    public ResponseEntity<?> getCandidateById(@RequestParam int id) {
//        logger.info("Received request to fetch candidate with ID: {}", id);
//        try {
//            var candidateDto = candidateService.getCandidateById(id);
//            logger.debug("Successfully fetched candidate with ID: {}", id);
//            return new ResponseEntity<>(candidateDto, HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error occurred while fetching candidate with ID: {}", id, e);
//            return ResponseEntity.internalServerError().body("Error fetching candidate: " + e.getMessage());
//        }
//    }
//
//    // ✅ Excel Upload API with comprehensive validation and processing
//    @PostMapping(value = "/candidate/upload",consumes = "multipart/form-data")
//    public ResponseEntity<?> uploadExcel(@RequestPart MultipartFile file) {
//        logger.info("Received Excel upload request with file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
//        try {
//            CandidateService.ExcelUploadResult result = candidateService.saveCandidatesFromExcel(file);
//
//            if (!result.getErrors().isEmpty()) {
//                logger.warn("Excel validation failed for file: {}. Errors: {}", file.getOriginalFilename(), result.getErrors());
//                return ResponseEntity.badRequest().body(result);
//            }
//
//            logger.info("Excel file processed successfully. Total: {}, Saved: {}, Updated: {}, Rejected: {}",
//                    result.getTotalRecords(), result.getSavedRecords(), result.getMergedRecords(), result.getRejectedRecords());
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            logger.error("Error processing Excel file: {}", file.getOriginalFilename(), e);
//            return ResponseEntity.internalServerError()
//                    .body("Error processing file: " + e.getMessage());
//        }
//    }
//
//}