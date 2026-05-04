package com.cts.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.cts.dto.CandidateDto;
import com.cts.entity.User;
import com.cts.exceptions.CandidateNotFoundException;
import com.cts.mapper.CandidateRowMapper;
import com.cts.repository.UserRepository;
import com.cts.util.CandidateExcelHelper;
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
    
    @Value("${app.pagination.page-size:10}")
    private int defaultPageSize;

    @Autowired
    public CandidateService(CandidateRepository candidateRepository,
                            UserRepository userRepository,
                            CandidateRowMapper candidateRowMapper,
                            PasswordEncoder passwordEncoder) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.candidateRowMapper = candidateRowMapper;
        this.passwordEncoder = passwordEncoder;
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
    public CandidateDto getCandidateById(int candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
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

    //Excel Upload Logic with validation, batch processing, and duplicate handling
    @Transactional
    public ExcelUploadResult saveCandidatesFromExcel(MultipartFile file) {
        ExcelUploadResult result = new ExcelUploadResult();

        try {
            InputStream is = file.getInputStream();

            // Step 1: Validate schema
            CandidateExcelHelper.ValidationResult validation = CandidateExcelHelper.validateExcelSchema(is);
            result.setSchemaValidationMessage(validation.getMessage());

            if (!validation.isValid()) {
                result.getErrors().add(validation.getMessage());
                return result;
            }

            // Reset input stream for parsing
            is = file.getInputStream();

            // Step 2: Parse candidates
            List<Candidate> candidates = CandidateExcelHelper.excelToCandidates(is);
            result.setTotalRecords(candidates.size());

            // Step 3: Process in batches of 50
            final int BATCH_SIZE = 50;
            List<Candidate> toSave = new ArrayList<>();
            List<User> users = new ArrayList<>();
            List<Candidate> toUpdate = new ArrayList<>();

            for (int i = 0; i < candidates.size(); i++) {
                Candidate candidate = candidates.get(i);

                // Check for duplicates
                Optional<Candidate> existing = candidateRepository.findById(candidate.getCognizantCandidateId());

                if (existing.isPresent()) {
                    // Check if data has changed
                    if (hasChanges(existing.get(), candidate)) {
                        // Merge changes
                        Candidate merged = mergeCandidates(existing.get(), candidate);
                        toUpdate.add(merged);
                        result.setMergedRecords(result.getMergedRecords() + 1);
                    } else {
                        // Exact duplicate, reject
                        result.setRejectedRecords(result.getRejectedRecords() + 1);
                    }
                } else {
                    // New candidate
                    User user = new User();
                    user.setPassword(passwordEncoder.encode(("welcome")));
                    user.setEmail(candidate.getCognizantEmailID());
                    user.setRole(User.Role.ROLE_TRAINEE);
                    users.add(user);
                    toSave.add(candidate);
                }

                // Process batch when it reaches BATCH_SIZE or at the end
                if (toSave.size() >= BATCH_SIZE || i == candidates.size() - 1) {
                    if (!toSave.isEmpty()) {
                        candidateRepository.saveAll(toSave);
                        userRepository.saveAll(users);
                        result.setSavedRecords(result.getSavedRecords() + toSave.size());
                        toSave.clear();
                    }
                }

                // Process updates in batches too
                if (toUpdate.size() >= BATCH_SIZE || i == candidates.size() - 1) {
                    if (!toUpdate.isEmpty()) {
                        candidateRepository.saveAll(toUpdate);
                        userRepository.saveAll(users);
                        toUpdate.clear();
                    }
                }
            }

        } catch (Exception e) {
            result.getErrors().add("Failed to process Excel file: " + e.getMessage());
        }

        return result;
    }


    @Transactional
    public void deleteCandidateById(Integer candidateId) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new CandidateNotFoundException("Candidate not found with id: " + candidateId));

        // This single line triggers cascading delete
        candidateRepository.delete(candidate);
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