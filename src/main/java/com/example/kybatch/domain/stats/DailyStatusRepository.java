package com.example.kybatch.domain.stats;

import com.example.kybatch.dto.DailyAggregationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyStatusRepository extends JpaRepository<DailyStatus, Long> {

    Optional<DailyStatus> findByUserIdAndDate(Long userId, LocalDate date);

    // ✅ (1) 기존 데이터 삭제 (Idempotent)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByDate(LocalDate date);

    // ✅ (2) Raw(UserActivity.createdAt) -> Daily 집계
    @Query("""
        SELECT new com.example.kybatch.dto.DailyAggregationDTO(
            ua.userId,
            SUM(ua.loginCount),
            SUM(ua.viewCount),
            SUM(ua.orderCount)
        )
        FROM UserActivity ua
        WHERE ua.createdAt >= :start
          AND ua.createdAt < :end
        GROUP BY ua.userId
    """)
    List<DailyAggregationDTO> aggregateDaily(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
