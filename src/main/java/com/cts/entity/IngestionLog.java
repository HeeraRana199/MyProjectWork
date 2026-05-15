package com.cts.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Audit record for one Excel upload attempt. One row per upload (success,
 * partial, or failed) so the Admin → Ingestion Logs tab can list every
 * historical attempt and let the operator drill into the reasons for failure.
 */
@Entity
@Table(name = "ingestion_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngestionLog {

    public enum Status { SUCCESS, PARTIAL, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    /** Set the moment the upload begins, even before any parsing. */
    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    /** Email of the admin who triggered the upload (best-effort, may be null). */
    private String uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    private int totalRecords;
    private int savedRecords;
    private int mergedRecords;
    private int rejectedRecords;

    /** Headline summary — e.g. "Missing 3 required column(s): ...". */
    @Column(length = 2000)
    private String schemaValidationMessage;

    /**
     * Errors are loaded explicitly via the /ingestion-logs/{id} endpoint so the
     * list endpoint doesn't trigger lazy-loading exceptions during serialization.
     */
    @OneToMany(mappedBy = "ingestionLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<IngestionError> errors = new ArrayList<>();
}
