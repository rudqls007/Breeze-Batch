package com.example.kybatch.domain.batchlog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobLogRepository extends JpaRepository<BatchJobLog, Long> {
}
