package com.cts.service;

import com.cts.entity.IngestionError;
import com.cts.entity.IngestionLog;
import com.cts.repository.IngestionErrorRepository;
import com.cts.repository.IngestionLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages IngestionLog + IngestionError lifecycle.
 *
 * Two resilience guarantees:
 *  - Propagation.REQUIRES_NEW so the audit trail survives a rollback of the outer
 *    upload transaction.
 *  - Every write is wrapped in try/catch — if the audit-log table itself fails
 *    (corrupted schema, lost DB connection, etc.) the upload still reports its
 *    real reason instead of a noisy SQL exception.
 */
@Service
@RequiredArgsConstructor
public class IngestionLogService {

    private static final Logger log = LoggerFactory.getLogger(IngestionLogService.class);

    private final IngestionLogRepository logRepository;
    private final IngestionErrorRepository errorRepository;

    /** Create the log row at the very start of an upload so it's tracked even if parsing throws. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IngestionLog startLog(String fileName, String uploadedBy) {
        try {
            IngestionLog entry = new IngestionLog();
            entry.setFileName(fileName == null ? "(unknown)" : fileName);
            entry.setUploadedAt(LocalDateTime.now());
            entry.setUploadedBy(uploadedBy);
            entry.setStatus(IngestionLog.Status.FAILED); // pessimistic default — overwritten on success
            return logRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to start ingestion log for file '{}': {}", fileName, e.getMessage());
            return null;
        }
    }

    /** One row per schema-missing column. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void appendSchemaError(Long logId, String missingColumn) {
        if (logId == null) return;
        try {
            IngestionLog ref = logRepository.getReferenceById(logId);
            IngestionError err = new IngestionError();
            err.setIngestionLog(ref);
            err.setErrorType(IngestionError.ErrorType.SCHEMA);
            err.setColumnName(missingColumn);
            err.setErrorReason("Missing required column: " + missingColumn);
            errorRepository.save(err);
        } catch (Exception e) {
            log.error("Failed to persist schema error for log {} column '{}': {}",
                    logId, missingColumn, e.getMessage());
        }
    }

    /** One row per Excel data row that failed validation (column issues are joined into errorReason). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void appendDataError(Long logId,
                                int rowNumber,
                                Integer associateId,
                                String candidateName,
                                List<String> failingColumns,
                                String reasonJoined) {
        if (logId == null) return;
        try {
            IngestionLog ref = logRepository.getReferenceById(logId);
            IngestionError err = new IngestionError();
            err.setIngestionLog(ref);
            err.setErrorType(IngestionError.ErrorType.DATA);
            err.setRowNumber(rowNumber);
            err.setAssociateId(associateId);
            err.setCandidateName(candidateName);
            err.setColumnName(failingColumns == null || failingColumns.isEmpty()
                    ? null : String.join(", ", failingColumns));
            err.setErrorReason(truncate(reasonJoined, 2000));
            errorRepository.save(err);
        } catch (Exception e) {
            log.error("Failed to persist data error for log {} row {}: {}",
                    logId, rowNumber, e.getMessage());
        }
    }

    /** Catch-all for unexpected exceptions during row processing. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void appendProcessingError(Long logId, int rowNumber, String reason) {
        if (logId == null) return;
        try {
            IngestionLog ref = logRepository.getReferenceById(logId);
            IngestionError err = new IngestionError();
            err.setIngestionLog(ref);
            err.setErrorType(IngestionError.ErrorType.PROCESSING);
            err.setRowNumber(rowNumber);
            err.setErrorReason(truncate("Processing error: " + reason, 2000));
            errorRepository.save(err);
        } catch (Exception e) {
            log.error("Failed to persist processing error for log {} row {}: {}",
                    logId, rowNumber, e.getMessage());
        }
    }

    /** Final status update with counts; called once at the end of the upload. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeLog(Long logId,
                            IngestionLog.Status status,
                            int total, int saved, int merged, int rejected,
                            String schemaValidationMessage) {
        if (logId == null) return;
        try {
            IngestionLog entry = logRepository.findById(logId).orElse(null);
            if (entry == null) {
                log.warn("IngestionLog {} not found while finalizing", logId);
                return;
            }
            entry.setStatus(status);
            entry.setTotalRecords(total);
            entry.setSavedRecords(saved);
            entry.setMergedRecords(merged);
            entry.setRejectedRecords(rejected);
            if (schemaValidationMessage != null) {
                entry.setSchemaValidationMessage(truncate(schemaValidationMessage, 2000));
            }
            logRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to finalize ingestion log {}: {}", logId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<IngestionLog> getAllLogs() {
        return logRepository.findAllByOrderByUploadedAtDesc();
    }

    @Transactional(readOnly = true)
    public IngestionLog getLog(Long id) {
        return logRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<IngestionError> getErrorsForLog(Long logId) {
        return errorRepository.findByIngestionLog_IdOrderByIdAsc(logId);
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
