package com.example.kybatch.domain.batchlog;

import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchJobLogRepository extends JpaRepository<BatchJobLog, Long> {

    @Query(value = """
        SELECT * 
        FROM batch_job_log
        WHERE (:jobName IS NULL OR job_name = :jobName)
        ORDER BY id DESC
        LIMIT 50
    """, nativeQuery = true)
    List<BatchJobLog> findRecentLogs(@Param("jobName") String jobName);
}

