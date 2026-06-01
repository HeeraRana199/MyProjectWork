package com.cts.service;

import com.cts.entity.IngestionError;
import com.cts.entity.IngestionLog;
import com.cts.repository.IngestionErrorRepository;
import com.cts.repository.IngestionLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionLogServiceTest {

    @Mock IngestionLogRepository logRepository;
    @Mock IngestionErrorRepository errorRepository;
    @InjectMocks IngestionLogService ingestionLogService;

    // ── startLog ────────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ startLog() creates a FAILED-by-default log row and returns it")
    void start_log_happy_path() {
        when(logRepository.save(any(IngestionLog.class))).thenAnswer(inv -> {
            IngestionLog l = inv.getArgument(0);
            l.setId(42L);
            return l;
        });

        IngestionLog result = ingestionLogService.startLog("candidates.xlsx", "admin@cts.com");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getFileName()).isEqualTo("candidates.xlsx");
        assertThat(result.getUploadedBy()).isEqualTo("admin@cts.com");
        assertThat(result.getStatus()).isEqualTo(IngestionLog.Status.FAILED); // pessimistic default
        assertThat(result.getUploadedAt()).isNotNull();
    }

    @Test
    @DisplayName("✓ startLog() defaults file name to '(unknown)' when null")
    void start_log_null_filename() {
        when(logRepository.save(any(IngestionLog.class))).thenAnswer(inv -> inv.getArgument(0));

        IngestionLog result = ingestionLogService.startLog(null, "x");

        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("(unknown)");
    }

    @Test
    @DisplayName("✗ startLog() returns null when the audit-log table is unreachable (swallowed)")
    void start_log_swallows_persistence_error() {
        when(logRepository.save(any(IngestionLog.class)))
                .thenThrow(new RuntimeException("DB down"));

        IngestionLog result = ingestionLogService.startLog("x.xlsx", "u");

        assertThat(result).isNull(); // no exception propagated
    }

    // ── appendSchemaError ────────────────────────────────────────────────

    @Test
    @DisplayName("✓ appendSchemaError() saves an IngestionError with type SCHEMA")
    void append_schema_error_persists() {
        IngestionLog ref = new IngestionLog(); ref.setId(1L);
        when(logRepository.getReferenceById(1L)).thenReturn(ref);

        ingestionLogService.appendSchemaError(1L, "Associate Id");

        ArgumentCaptor<IngestionError> cap = ArgumentCaptor.forClass(IngestionError.class);
        verify(errorRepository, times(1)).save(cap.capture());
        IngestionError saved = cap.getValue();
        assertThat(saved.getErrorType()).isEqualTo(IngestionError.ErrorType.SCHEMA);
        assertThat(saved.getColumnName()).isEqualTo("Associate Id");
        assertThat(saved.getErrorReason()).contains("Associate Id");
    }

    @Test
    @DisplayName("✗ appendSchemaError() is a no-op when logId is null")
    void append_schema_error_null_log_id() {
        ingestionLogService.appendSchemaError(null, "Email");

        verify(errorRepository, never()).save(any(IngestionError.class));
    }

    @Test
    @DisplayName("✗ appendSchemaError() swallows persistence failures")
    void append_schema_error_swallows_failure() {
        when(logRepository.getReferenceById(1L)).thenThrow(new RuntimeException("boom"));

        // Must not throw
        ingestionLogService.appendSchemaError(1L, "Email");
    }

    // ── appendDataError ──────────────────────────────────────────────────

    @Test
    @DisplayName("✓ appendDataError() persists row identity, columns, and joined reason")
    void append_data_error_persists() {
        IngestionLog ref = new IngestionLog(); ref.setId(7L);
        when(logRepository.getReferenceById(7L)).thenReturn(ref);

        ingestionLogService.appendDataError(7L, 5, 12345, "Alice",
                List.of("Email", "Gender"), "email invalid; gender unknown");

        ArgumentCaptor<IngestionError> cap = ArgumentCaptor.forClass(IngestionError.class);
        verify(errorRepository).save(cap.capture());
        IngestionError saved = cap.getValue();
        assertThat(saved.getErrorType()).isEqualTo(IngestionError.ErrorType.DATA);
        assertThat(saved.getRowNumber()).isEqualTo(5);
        assertThat(saved.getAssociateId()).isEqualTo(12345);
        assertThat(saved.getCandidateName()).isEqualTo("Alice");
        assertThat(saved.getColumnName()).isEqualTo("Email, Gender");
        assertThat(saved.getErrorReason()).contains("invalid");
    }

    @Test
    @DisplayName("✓ appendDataError() leaves columnName null when no failing columns are supplied")
    void append_data_error_null_columns() {
        IngestionLog ref = new IngestionLog(); ref.setId(7L);
        when(logRepository.getReferenceById(7L)).thenReturn(ref);

        ingestionLogService.appendDataError(7L, 5, 1, "Bob", null, "some reason");

        ArgumentCaptor<IngestionError> cap = ArgumentCaptor.forClass(IngestionError.class);
        verify(errorRepository).save(cap.capture());
        assertThat(cap.getValue().getColumnName()).isNull();
    }

    @Test
    @DisplayName("✗ appendDataError() is a no-op when logId is null")
    void append_data_error_null_log_id() {
        ingestionLogService.appendDataError(null, 2, 1, "X", List.of("A"), "r");

        verify(errorRepository, never()).save(any(IngestionError.class));
    }

    // ── appendProcessingError ────────────────────────────────────────────

    @Test
    @DisplayName("✓ appendProcessingError() persists with type PROCESSING and prefixed reason")
    void append_processing_error_persists() {
        IngestionLog ref = new IngestionLog(); ref.setId(9L);
        when(logRepository.getReferenceById(9L)).thenReturn(ref);

        ingestionLogService.appendProcessingError(9L, 3, "NullPointerException");

        ArgumentCaptor<IngestionError> cap = ArgumentCaptor.forClass(IngestionError.class);
        verify(errorRepository).save(cap.capture());
        IngestionError saved = cap.getValue();
        assertThat(saved.getErrorType()).isEqualTo(IngestionError.ErrorType.PROCESSING);
        assertThat(saved.getRowNumber()).isEqualTo(3);
        assertThat(saved.getErrorReason()).startsWith("Processing error: ");
        assertThat(saved.getErrorReason()).contains("NullPointerException");
    }

    @Test
    @DisplayName("✗ appendProcessingError() is a no-op when logId is null")
    void append_processing_error_null_log_id() {
        ingestionLogService.appendProcessingError(null, 1, "x");

        verify(errorRepository, never()).save(any(IngestionError.class));
    }

    // ── finalizeLog ──────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ finalizeLog() updates status and all four counts on the existing log")
    void finalize_log_happy_path() {
        IngestionLog existing = new IngestionLog();
        existing.setId(1L);
        existing.setStatus(IngestionLog.Status.FAILED);
        when(logRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(logRepository.save(any(IngestionLog.class))).thenAnswer(inv -> inv.getArgument(0));

        ingestionLogService.finalizeLog(1L, IngestionLog.Status.SUCCESS,
                100, 80, 10, 10, "Schema OK");

        assertThat(existing.getStatus()).isEqualTo(IngestionLog.Status.SUCCESS);
        assertThat(existing.getTotalRecords()).isEqualTo(100);
        assertThat(existing.getSavedRecords()).isEqualTo(80);
        assertThat(existing.getMergedRecords()).isEqualTo(10);
        assertThat(existing.getRejectedRecords()).isEqualTo(10);
        assertThat(existing.getSchemaValidationMessage()).isEqualTo("Schema OK");
        verify(logRepository).save(existing);
    }

    @Test
    @DisplayName("✗ finalizeLog() is a no-op when logId is null")
    void finalize_log_null_id() {
        ingestionLogService.finalizeLog(null, IngestionLog.Status.SUCCESS, 0, 0, 0, 0, null);

        verify(logRepository, never()).save(any(IngestionLog.class));
    }

    @Test
    @DisplayName("✗ finalizeLog() is a no-op (warns) when the log id can't be found")
    void finalize_log_not_found() {
        when(logRepository.findById(99L)).thenReturn(Optional.empty());

        ingestionLogService.finalizeLog(99L, IngestionLog.Status.SUCCESS, 0, 0, 0, 0, null);

        verify(logRepository, never()).save(any(IngestionLog.class));
    }

    @Test
    @DisplayName("✗ finalizeLog() swallows persistence failures")
    void finalize_log_swallows_failure() {
        when(logRepository.findById(1L)).thenThrow(new RuntimeException("DB down"));

        // Must not throw
        ingestionLogService.finalizeLog(1L, IngestionLog.Status.SUCCESS, 0, 0, 0, 0, null);
    }

    // ── read endpoints ───────────────────────────────────────────────────

    @Test
    @DisplayName("✓ getAllLogs() returns logs ordered most-recent first")
    void get_all_logs_delegates() {
        IngestionLog l1 = new IngestionLog(); l1.setId(1L);
        IngestionLog l2 = new IngestionLog(); l2.setId(2L);
        when(logRepository.findAllByOrderByUploadedAtDesc()).thenReturn(List.of(l2, l1));

        List<IngestionLog> result = ingestionLogService.getAllLogs();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("✓ getLog() returns persisted log when present")
    void get_log_found() {
        IngestionLog l = new IngestionLog(); l.setId(5L);
        when(logRepository.findById(5L)).thenReturn(Optional.of(l));

        IngestionLog result = ingestionLogService.getLog(5L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("✗ getLog() returns null (no throw) when missing")
    void get_log_not_found() {
        when(logRepository.findById(404L)).thenReturn(Optional.empty());

        assertThat(ingestionLogService.getLog(404L)).isNull();
    }

    @Test
    @DisplayName("✓ getErrorsForLog() returns errors for the given log id")
    void get_errors_for_log_delegates() {
        IngestionError e1 = new IngestionError(); e1.setId(11L);
        when(errorRepository.findByIngestionLog_IdOrderByIdAsc(7L)).thenReturn(List.of(e1));

        List<IngestionError> result = ingestionLogService.getErrorsForLog(7L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(11L);
    }
}
