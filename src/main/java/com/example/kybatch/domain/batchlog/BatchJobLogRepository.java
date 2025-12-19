package com.example.kybatch.domain.batchlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BatchJobLogRepository extends JpaRepository<BatchJobLog, Long> {

    @Query(value = """
        SELECT * 
        FROM batch_job_log
        WHERE (:jobName IS NULL OR job_name = :jobName)
        ORDER BY id DESC
        LIMIT 50
    """, nativeQuery = true)
    List<BatchJobLog> findRecentLogs(@Param("jobName") String jobName);

    /**
     * STEP 35 대비: 특정 originJobExecutionId 기준으로 Admin 재실행 이력 조회
     */
    @Query(value = """
        SELECT *
        FROM batch_job_log
        WHERE origin_job_execution_id = :originJobExecutionId
          AND execute_type = 'ADMIN_RESTART'
        ORDER BY id DESC
    """, nativeQuery = true)
    List<BatchJobLog> findAdminRestartLogs(@Param("originJobExecutionId") Long originJobExecutionId);
}
