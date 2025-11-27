package com.example.kybatch.domain.stats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * DailyStatusRepository
 * - 집계 결과를 검증하거나, 리포트용으로 조회할 때 사용
 */
public interface DailyStatusRepository extends JpaRepository<DailyStatus, Long> {

    Optional<DailyStatus> findByUserIdAndDate(Long userId, LocalDate date);
}