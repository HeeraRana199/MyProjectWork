package com.cts.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single error attached to an {@link IngestionLog}. Two kinds:
 *  - SCHEMA: a required column was missing from the uploaded file
 *  - DATA:   a row failed data validation (gender invalid, email malformed, etc.)
 *  - PROCESSING: an unexpected exception while processing a row
 */
@Entity
@Table(name = "ingestion_error")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngestionError {

    public enum ErrorType { SCHEMA, DATA, PROCESSING }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingestion_log_id", nullable = false)
    @JsonBackReference
    private IngestionLog ingestionLog;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ErrorType errorType;

    /**
     * Excel row number (1-indexed; row 1 is the header). Null for schema-level errors.
     * NB: column is named excel_row_number — `row_number` is a reserved word in MySQL 8+
     * (the window function), and Hibernate's generated INSERT would otherwise fail with a syntax error.
     */
    @Column(name = "excel_row_number")
    private Integer rowNumber;

    /** For schema errors: the missing column name. For data errors: comma-separated fields. */
    private String columnName;

    /** Best-effort row identity for data errors. */
    private Integer associateId;
    private String candidateName;

    @Column(length = 2000, nullable = false)
    private String errorReason;
}
