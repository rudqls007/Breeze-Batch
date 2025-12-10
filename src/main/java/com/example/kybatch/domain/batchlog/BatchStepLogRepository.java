package com.example.kybatch.domain.batchlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchStepLogRepository extends JpaRepository<BatchStepLog, Long> {

    @Query(value = """
        SELECT * 
        FROM batch_step_log
        WHERE (:stepName IS NULL OR step_name = :stepName)
        ORDER BY id DESC
        LIMIT 50
    """, nativeQuery = true)
    List<BatchStepLog> findRecentLogs(@Param("stepName") String stepName);

}
