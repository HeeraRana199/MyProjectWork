package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cts.entity.Candidate;
import com.cts.service.CandidateService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class CandidateController {

    private CandidateService candidateService;

    @PostMapping("/candidate")
    public ResponseEntity<?> addCandidate(@RequestBody Candidate candidate) {
        return new ResponseEntity<>(candidateService.addCandidate(candidate), HttpStatus.CREATED);
    }

    @GetMapping("/candidate")
    public ResponseEntity<?> getCandidateById(@RequestParam int id) {
        return new ResponseEntity<>(candidateService.getCandidateById(id), HttpStatus.OK);
    }

    // ✅ Excel Upload API with comprehensive validation and processing
    @PostMapping(value = "/candidate/upload",consumes = "multipart/form-data")
    public ResponseEntity<?> uploadExcel(@RequestPart MultipartFile file) {
        try {
            CandidateService.ExcelUploadResult result = candidateService.saveCandidatesFromExcel(file);

            if (!result.getErrors().isEmpty()) {
                return ResponseEntity.badRequest().body(result);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error processing file: " + e.getMessage());
        }
    }

}