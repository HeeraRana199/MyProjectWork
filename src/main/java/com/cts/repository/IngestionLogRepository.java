package com.cts.repository;

import com.cts.entity.IngestionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngestionLogRepository extends JpaRepository<IngestionLog, Long> {
    /** Most recent uploads first — what the Ingestion Logs page wants. */
    List<IngestionLog> findAllByOrderByUploadedAtDesc();
}
