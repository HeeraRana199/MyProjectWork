package com.cts.controller;

import com.cts.entity.Candidate;
import com.cts.entity.IngestionError;
import com.cts.entity.IngestionLog;
import com.cts.entity.User;
import com.cts.service.AuthService;
import com.cts.service.CandidateService;
import com.cts.service.IngestionLogService;
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
import java.util.List;

@RestController
//@AllArgsConstructor
@RequiredArgsConstructor
@RequestMapping("/admin")
//@CrossOrigin(origins = "http://localhost:5173") // Vite frontend
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final CandidateService candidateService;
    private final AuthService authService;
    private final IngestionLogService ingestionLogService;

    @Value("${file.upload-dir}")
    private String uploadDir;


    @PostMapping("/leaderRegister")
    public ResponseEntity<?> leaderRegister(@RequestBody User user) {
        try {
            User savedUser = authService.leaderRegister(user);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (org.springframework.dao.DataIntegrityViolationException dive) {
            // Most likely a duplicate email (unique constraint).
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("message", "A leader with that email already exists."));
        } catch (Exception e) {
            logger.error("Failed to register leader: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("message", "Failed to register leader: " + e.getMessage()));
        }
    }

    /** List every leader account (most recent first). */
    @GetMapping("/leaders")
    public ResponseEntity<?> listLeaders() {
        try {
            return ResponseEntity.ok(authService.listLeaders());
        } catch (Exception e) {
            logger.error("Failed to fetch leaders", e);
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("message", "Failed to fetch leaders: " + e.getMessage()));
        }
    }

    /** Delete a leader account by its user id. Rejects if the account isn't a leader. */
    @DeleteMapping("/leader/{userId}")
    public ResponseEntity<?> deleteLeader(@PathVariable Long userId) {
        try {
            authService.deleteLeader(userId);
            return ResponseEntity.ok(java.util.Map.of("userId", userId, "message", "Leader deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to delete leader {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("message", "Failed to delete leader: " + e.getMessage()));
        }
    }


    // ✅ Excel Upload API with comprehensive validation and processing
    @PostMapping(value = "/candidate/upload",consumes = "multipart/form-data")
    public ResponseEntity<?> uploadExcel(@RequestPart MultipartFile file) {
        logger.info("Received Excel upload request with file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        try {
            // Best-effort attribution: pull the admin's email from the SecurityContext if present.
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String uploadedBy = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;

            CandidateService.ExcelUploadResult result = candidateService.saveCandidatesFromExcel(file, uploadedBy);

            // Classify the outcome:
            //  - Nothing was even parseable (schema invalid / unreadable file) → 400
            //  - All rows rejected (zero saved + zero merged) → 400
            //  - At least one row saved or merged → 200, even if some rows were
            //    rejected as duplicates / invalid. Per-row errors travel back
            //    inside result.errors[] and the frontend renders them in the
            //    amber "rows had issues" section under the green stats card.
            int succeeded = result.getSavedRecords() + result.getMergedRecords();
            boolean nothingProcessed = result.getTotalRecords() == 0 && !result.getErrors().isEmpty();
            boolean allRejected = result.getTotalRecords() > 0 && succeeded == 0;

            if (nothingProcessed || allRejected) {
                logger.warn("Excel upload failed for file: {}. saved={}, merged={}, rejected={}, errors={}",
                        file.getOriginalFilename(),
                        result.getSavedRecords(), result.getMergedRecords(),
                        result.getRejectedRecords(), result.getErrors());
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
    public ResponseEntity<?> getAssociateById(@RequestParam int id) {
        logger.info("Received request to fetch candidate with ID: {}", id);
        try {
            var candidateDto = candidateService.getAssociateById(id);
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

    // ✅ Fetch image by Associate
    @GetMapping("/profile-photo/{associateId}")
    public ResponseEntity<Resource> getProfileImage(
            @PathVariable Long associateId
    ) throws MalformedURLException {

        Path imagePath = Paths.get(uploadDir).resolve(associateId + ".jpg");

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

    // ── Ingestion Logs (Admin → Ingestion Logs tab) ───────────────────────────

    /** List every upload attempt — most recent first. */
    @GetMapping("/ingestion-logs")
    public ResponseEntity<?> listIngestionLogs() {
        try {
            List<IngestionLog> logs = ingestionLogService.getAllLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Error fetching ingestion logs", e);
            return ResponseEntity.internalServerError().body("Error fetching ingestion logs: " + e.getMessage());
        }
    }

    /** Drill-down for one upload: returns the log summary + its full error list. */
    @GetMapping("/ingestion-logs/{id}")
    public ResponseEntity<?> getIngestionLogDetails(@PathVariable Long id) {
        try {
            IngestionLog log = ingestionLogService.getLog(id);
            if (log == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("message", "Ingestion log not found"));
            }
            List<IngestionError> errors = ingestionLogService.getErrorsForLog(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "log", log,
                    "errors", errors
            ));
        } catch (Exception e) {
            logger.error("Error fetching ingestion log {}", id, e);
            return ResponseEntity.internalServerError().body("Error fetching ingestion log: " + e.getMessage());
        }
    }

    @DeleteMapping("/candidate/{associateId}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Integer associateId) {
        logger.info("Received request to delete candidate with ID: {}", associateId);

        try {
            candidateService.deleteCandidateById(associateId);
            logger.info("Successfully deleted candidate with ID: {}", associateId);

            return ResponseEntity.ok("Candidate and all associated data deleted successfully");

        } catch (Exception e) {
            logger.error("Error occurred while deleting candidate with ID: {}", associateId, e);
            return ResponseEntity.internalServerError()
                    .body("Error deleting candidate: " + e.getMessage());
        }
    }
}
