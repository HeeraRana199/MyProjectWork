package com.cts.controller;

import com.cts.entity.Candidate;
import com.cts.entity.User;
import com.cts.service.AuthService;
import com.cts.service.CandidateService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
//@AllArgsConstructor
@RequiredArgsConstructor
@RequestMapping("/admin")
//@CrossOrigin(origins = "http://localhost:5173") // Vite frontend
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final CandidateService candidateService;
    private final AuthService authService;

    @Value("${file.upload-dir}")
    private String uploadDir;


    @PostMapping("/leaderRegister")
    public ResponseEntity<User> leaderRegister(@RequestBody User user) {
        // Authenticate using Spring Security (validates username + password via BCrypt)
        User savedUser = authService.leaderRegister(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }


    // ✅ Excel Upload API with comprehensive validation and processing
    @PostMapping(value = "/candidate/upload",consumes = "multipart/form-data")
    public ResponseEntity<?> uploadExcel(@RequestPart MultipartFile file) {
        logger.info("Received Excel upload request with file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        try {
            CandidateService.ExcelUploadResult result = candidateService.saveCandidatesFromExcel(file);

            if (!result.getErrors().isEmpty()) {
                logger.warn("Excel validation failed for file: {}. Errors: {}", file.getOriginalFilename(), result.getErrors());
                System.out.println(result);
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
    public ResponseEntity<?> getAllCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer pageSize) {
        logger.info("Received request to fetch candidates - page: {}, pageSize: {}", page, pageSize);
        try {
            var paginatedCandidates = candidateService.getAllCandidatesPaginated(page, pageSize);
            logger.debug("Successfully fetched page {} with candidates", page);
            return new ResponseEntity<>(paginatedCandidates, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching paginated candidates", e);
            return ResponseEntity.internalServerError().body("Error fetching candidates: " + e.getMessage());
        }
    }

    // ✅ Fetch image by candidateId
    @GetMapping("/profile-photo/{candidateId}")
    public ResponseEntity<Resource> getProfileImage(
            @PathVariable Long candidateId
    ) throws MalformedURLException {

        Path imagePath = Paths.get(uploadDir).resolve(candidateId + ".jpg");

        if (!Files.exists(imagePath)) {

            //return ResponseEntity.notFound().build();
            Path defaultPath = Paths.get(uploadDir).resolve("000000.jpg");
            Resource resource = new UrlResource(defaultPath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        }

        Resource resource = new UrlResource(imagePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
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
