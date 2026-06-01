package com.cts.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.cts.dto.CandidateDto;
import com.cts.entity.IngestionLog;
import com.cts.entity.User;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.mapper.CandidateRowMapper;
import com.cts.repository.UserRepository;
import com.cts.util.CandidateExcelHelper;
import com.cts.util.CandidateValidator;
import com.cts.util.CandidateValidator.FieldError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cts.entity.Candidate;
import com.cts.repository.CandidateRepository;

@Service
public class CandidateService {

    private CandidateRepository candidateRepository;
    private UserRepository userRepository;
    private CandidateRowMapper candidateRowMapper;
    private PasswordEncoder passwordEncoder;
    private IngestionLogService ingestionLogService;
    private CandidateBatchHelper candidateBatchHelper;

    @Value("${app.pagination.page-size:10}")
    private int defaultPageSize;

    @Autowired
    public CandidateService(CandidateRepository candidateRepository,
                            UserRepository userRepository,
                            CandidateRowMapper candidateRowMapper,
                            PasswordEncoder passwordEncoder,
                            IngestionLogService ingestionLogService,
                            CandidateBatchHelper candidateBatchHelper) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.candidateRowMapper = candidateRowMapper;
        this.passwordEncoder = passwordEncoder;
        this.candidateBatchHelper = candidateBatchHelper;
        this.ingestionLogService = ingestionLogService;
    }

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

    public static class PaginatedCandidatesResponse {
        private List<CandidateDto> content;
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean isLast;

        public PaginatedCandidatesResponse(List<CandidateDto> content, int currentPage, int pageSize,
                                           long totalElements, int totalPages, boolean isLast) {
            this.content = content;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.isLast = isLast;
        }

        // Getters
        public List<CandidateDto> getContent() { return content; }
        public int getCurrentPage() { return currentPage; }
        public int getPageSize() { return pageSize; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public boolean isLast() { return isLast; }
    }

    //Create candidate logic
    public Candidate addCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    //Get candidate info logic
    @Transactional
    public CandidateDto getAssociateById(int associateId) {
        Candidate candidate = candidateRepository.findById(associateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));

        return candidateRowMapper.convertToCandidateDto(candidate);
    }

    //Get all candidates logic
    @Transactional
    public List<CandidateDto> getAllCandidates() {
        List<Candidate> candidates = candidateRepository.findAll();
        return candidates.stream()
                .map(candidateRowMapper::convertToCandidateDto)
                .collect(Collectors.toList());
    }

    //Get paginated candidates logic
    @Transactional
    public PaginatedCandidatesResponse getAllCandidatesPaginated(int page, Integer pageSize) {
        int size = (pageSize != null && pageSize > 0) ? pageSize : defaultPageSize;
        Pageable pageable = PageRequest.of(page, size);
        Page<Candidate> candidatePage = candidateRepository.findAll(pageable);

        List<CandidateDto> content = candidatePage.getContent().stream()
                .map(candidateRowMapper::convertToCandidateDto)
                .collect(Collectors.toList());

        return new PaginatedCandidatesResponse(
                content,
                page,
                size,
                candidatePage.getTotalElements(),
                candidatePage.getTotalPages(),
                candidatePage.isLast()
        );
    }

    /**
     * Excel Upload Logic with schema validation, per-row data validation,
     * batch processing, duplicate handling, and full ingestion-log audit trail.
     *
     * Every upload attempt creates one IngestionLog row up front. Schema and
     * data validation failures are persisted as IngestionError children using
     * REQUIRES_NEW so the audit trail survives even if the main transaction
     * rolls back.
     */
    @Transactional
    public ExcelUploadResult saveCandidatesFromExcel(MultipartFile file) {
        return saveCandidatesFromExcel(file, null);
    }

    @Transactional
    public ExcelUploadResult saveCandidatesFromExcel(MultipartFile file, String uploadedBy) {
        ExcelUploadResult result = new ExcelUploadResult();
        String fileName = file.getOriginalFilename();

        // Create the ingestion log first so we have something to attach errors to.
        // startLog returns null if the audit-log table itself is unreachable — we
        // still let the upload proceed in that case (logId stays null and all
        // ingestionLogService.* calls below short-circuit safely).
        IngestionLog ingestionLog = ingestionLogService.startLog(fileName, uploadedBy);
        Long logId = ingestionLog == null ? null : ingestionLog.getId();

        try {
            InputStream is = file.getInputStream();

            // ── Step 1: Schema validation ─────────────────────────────────
            CandidateExcelHelper.ValidationResult validation = CandidateExcelHelper.validateExcelSchema(is);
            result.setSchemaValidationMessage(validation.getMessage());

            if (!validation.isValid()) {
                if (validation.getMissingHeaders().isEmpty()) {
                    result.getErrors().add(validation.getMessage());
                } else {
                    for (String h : validation.getMissingHeaders()) {
                        result.getErrors().add("Missing column: " + h);
                        ingestionLogService.appendSchemaError(logId, h);
                    }
                }
                ingestionLogService.finalizeLog(logId,
                        IngestionLog.Status.FAILED,
                        0, 0, 0, 0,
                        validation.getMessage());
                return result;
            }

            // ── Step 2: Parse candidates ──────────────────────────────────
            is = file.getInputStream();
            List<Candidate> candidates = CandidateExcelHelper.excelToCandidates(is);
            result.setTotalRecords(candidates.size());

            // ── Step 3: Per-row processing (validate → dedupe → save) ─────
            // Each row is saved in its OWN REQUIRES_NEW sub-transaction via
            // candidateBatchHelper. That means:
            //  - Unique rows commit independently. One row's failure cannot
            //    poison the rest of the upload with "rollback-only".
            //  - Within-batch duplicates are caught up-front and rejected
            //    cleanly before they ever hit the DB.
            //  - Pre-existing rows in the DB are merged in their own sub-txn.

            // Track IDs already accepted within THIS upload so a second row
            // with the same Associate Id / Email is rejected up-front rather
            // than tripping a SQL unique constraint.
            java.util.Set<Integer> seenAssociateIds = new java.util.HashSet<>();
            // java.util.Set<Integer> seenCognizantIds = new java.util.HashSet<>(); // Cognizant Candidate ID dedup commented out
            java.util.Set<String>  seenEmails       = new java.util.HashSet<>();

            for (int i = 0; i < candidates.size(); i++) {
                Candidate candidate = candidates.get(i);
                int rowNumber = i + 2; // Excel is 1-indexed; row 1 is the header

                try {
                    // ── 3a. Data validation ─────────────────────────────────
                    List<FieldError> fieldErrors = CandidateValidator.validate(candidate);
                    if (!fieldErrors.isEmpty()) {
                        List<String> columns = new ArrayList<>();
                        List<String> messages = new ArrayList<>();
                        for (FieldError fe : fieldErrors) {
                            columns.add(fe.column());
                            messages.add(fe.toString());
                        }
                        String joined = String.join("; ", messages);
                        ingestionLogService.appendDataError(logId, rowNumber,
                                candidate.getAssociateId(),
                                candidate.getCandidateName(),
                                columns, joined);
                        result.getErrors().add("Row " + rowNumber + ": " + joined);
                        result.setRejectedRecords(result.getRejectedRecords() + 1);
                        continue;
                    }

                    // ── 3b. Within-batch duplicate guard ───────────────────
                    Integer assocId = candidate.getAssociateId();
                    // Integer cogId   = candidate.getCognizantCandidateId(); // Cognizant Candidate ID commented out per project decision
                    String  email   = candidate.getCognizantEmailID() == null
                            ? null : candidate.getCognizantEmailID().trim().toLowerCase();

                    String duplicateReason = null;
                    String duplicateColumn = null;
                    if (assocId != null && seenAssociateIds.contains(assocId)) {
                        duplicateColumn = "Associate Id";
                        duplicateReason = "Duplicate Associate Id " + assocId
                                + " — already present earlier in this upload";
                    // } else if (cogId != null && seenCognizantIds.contains(cogId)) {
                    //     duplicateColumn = "Cognizant Candidate ID";
                    //     duplicateReason = "Duplicate Cognizant Candidate ID " + cogId
                    //             + " — already present earlier in this upload";
                    } else if (email != null && !email.isEmpty() && seenEmails.contains(email)) {
                        duplicateColumn = "Cognizant Email ID";
                        duplicateReason = "Duplicate Cognizant Email ID " + candidate.getCognizantEmailID()
                                + " — already present earlier in this upload";
                    }
                    if (duplicateReason != null) {
                        ingestionLogService.appendDataError(logId, rowNumber,
                                assocId,
                                candidate.getCandidateName(),
                                List.of(duplicateColumn),
                                duplicateReason);
                        result.getErrors().add("Row " + rowNumber + ": " + duplicateReason);
                        result.setRejectedRecords(result.getRejectedRecords() + 1);
                        continue;
                    }

                    // ── 3c. Decide: existing → merge, or new → insert ──────
                    Optional<Candidate> existing = candidateRepository.findById(candidate.getAssociateId());

                    boolean accepted = false;
                    Candidate toPersist = null;
                    String operation = "save";

                    if (existing.isPresent()) {
                        if (hasChanges(existing.get(), candidate)) {
                            toPersist = mergeCandidates(existing.get(), candidate);
                            operation = "merge";
                        } else {
                            // Exact match against DB → already saved; reject the duplicate
                            result.setRejectedRecords(result.getRejectedRecords() + 1);
                            // Still mark as "seen" so subsequent in-file dupes catch it too
                            if (assocId != null) seenAssociateIds.add(assocId);
                            // if (cogId != null) seenCognizantIds.add(cogId); // Cognizant Candidate ID commented out
                            if (email != null && !email.isEmpty()) seenEmails.add(email);
                            continue;
                        }
                    } else {
                        User user = new User();
                        user.setPassword(passwordEncoder.encode("welcome"));
                        user.setEmail(candidate.getCognizantEmailID());
                        user.setRole(User.Role.ROLE_TRAINEE);
                        user.setCandidate(candidate);
                        candidate.setUser(user);
                        toPersist = candidate;
                    }

                    // ── 3d. Persist in its OWN sub-transaction ─────────────
                    // If the DB rejects this row (e.g. concurrent insert by
                    // another upload, future schema constraint), only THIS
                    // sub-transaction rolls back. The loop keeps going.
                    try {
                        candidateBatchHelper.saveOne(toPersist);
                        accepted = true;
                        if ("merge".equals(operation)) {
                            result.setMergedRecords(result.getMergedRecords() + 1);
                        } else {
                            result.setSavedRecords(result.getSavedRecords() + 1);
                        }
                    } catch (Exception persistEx) {
                        // Unforeseen DB-level rejection (constraint violation, etc.).
                        // Walk to the root cause for a more useful message than the
                        // outer Hibernate wrapping ("could not execute statement").
                        Throwable root = persistEx;
                        while (root.getCause() != null && root.getCause() != root) {
                            root = root.getCause();
                        }
                        String reason = root.getMessage();
                        if (reason == null || reason.isBlank()) {
                            reason = root.getClass().getSimpleName();
                        }
                        ingestionLogService.appendProcessingError(logId, rowNumber, reason);
                        result.getErrors().add("Row " + rowNumber + ": Could not save — " + reason);
                        result.setRejectedRecords(result.getRejectedRecords() + 1);
                    }

                    // Mark IDs as seen ONLY when the row was actually accepted,
                    // so a failed save doesn't prevent a subsequent (potentially
                    // legitimate) row from re-trying.
                    if (accepted) {
                        if (assocId != null) seenAssociateIds.add(assocId);
                        // if (cogId != null) seenCognizantIds.add(cogId); // Cognizant Candidate ID commented out
                        if (email != null && !email.isEmpty()) seenEmails.add(email);
                    }
                } catch (Exception rowEx) {
                    // Non-DB exception in the row pipeline (validator throws, etc.)
                    ingestionLogService.appendProcessingError(logId, rowNumber, rowEx.getMessage());
                    result.getErrors().add("Row " + rowNumber + ": Processing error: " + rowEx.getMessage());
                    result.setRejectedRecords(result.getRejectedRecords() + 1);
                }
            }

        } catch (Exception e) {
            result.getErrors().add("Failed to process Excel file: " + e.getMessage());
            ingestionLogService.appendProcessingError(logId, 0, e.getMessage());
        }

        // ── Finalize the audit log with status + counts ────────────────────
        IngestionLog.Status finalStatus;
        if (result.getRejectedRecords() == 0 && (result.getSavedRecords() + result.getMergedRecords()) > 0) {
            finalStatus = IngestionLog.Status.SUCCESS;
        } else if (result.getSavedRecords() + result.getMergedRecords() > 0) {
            finalStatus = IngestionLog.Status.PARTIAL;
        } else {
            finalStatus = IngestionLog.Status.FAILED;
        }
        ingestionLogService.finalizeLog(logId, finalStatus,
                result.getTotalRecords(),
                result.getSavedRecords(),
                result.getMergedRecords(),
                result.getRejectedRecords(),
                result.getSchemaValidationMessage());

        return result;
    }


    @Transactional
    public void deleteCandidateById(Integer associateId) {

        Candidate candidate = candidateRepository.findById(associateId)
                .orElseThrow(() ->
                        new CandidateNotFoundException("Candidate not found with id: " + associateId));

        // This single line triggers cascading delete
        candidateRepository.delete(candidate);
    }


    private boolean hasChanges(Candidate existing, Candidate newCandidate) {
        return !equals(existing.getAssociateId(), newCandidate.getAssociateId()) ||
               !equals(existing.getCandidateName(), newCandidate.getCandidateName()) ||
               !equals(existing.getCognizantEmailID(), newCandidate.getCognizantEmailID()) ||
               !equals(existing.getGender(), newCandidate.getGender()) ||
               !equals(existing.getCohortCode(), newCandidate.getCohortCode()) ||
               !equals(existing.getSl(), newCandidate.getSl()) ||
               !equals(existing.getDeploymentLocation(), newCandidate.getDeploymentLocation()) ||
               !equals(existing.getTrackName(), newCandidate.getTrackName()) ||
               !equals(existing.getCandidateScore().getFinalScore(), newCandidate.getCandidateScore().getFinalScore()) ||
                !equals(existing.getCandidateScore().getInterimScore(), newCandidate.getCandidateScore().getInterimScore());
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
        if (newCandidate.getSl() != null && !newCandidate.getSl().isEmpty()) {
            existing.setSl(newCandidate.getSl());
        }
        if (newCandidate.getDeploymentLocation() != null && !newCandidate.getDeploymentLocation().isEmpty()) {
            existing.setDeploymentLocation(newCandidate.getDeploymentLocation());
        }
        if (newCandidate.getTrackName() != null && !newCandidate.getTrackName().isEmpty()) {
            existing.setTrackName(newCandidate.getTrackName());
        }
        if(newCandidate.getCandidateScore().getInterimScore() != null && !newCandidate.getCandidateScore().getInterimScore().isEmpty()){
            existing.getCandidateScore().setInterimScore(newCandidate.getCandidateScore().getInterimScore());
        }
        if(newCandidate.getCandidateScore().getFinalScore() != null && !newCandidate.getCandidateScore().getFinalScore().isEmpty()){
            existing.getCandidateScore().setFinalScore(newCandidate.getCandidateScore().getFinalScore());
            if(newCandidate.getCandidateScore().getFinalScore().equalsIgnoreCase("Green")){
                existing.getCandidateScore().setReadiness("Ready");
            }
            else if(newCandidate.getCandidateScore().getFinalScore().equalsIgnoreCase("Red")){
                existing.getCandidateScore().setReadiness("Not Ready");
            }
            else if(newCandidate.getCandidateScore().getFinalScore().equalsIgnoreCase("Amber")){
                existing.getCandidateScore().setReadiness("Not Ready");
            }
        }

        return existing;
    }

    private boolean equals(Object obj1, Object obj2) {
        return (obj1 == null && obj2 == null) || (obj1 != null && obj1.equals(obj2));
    }
}