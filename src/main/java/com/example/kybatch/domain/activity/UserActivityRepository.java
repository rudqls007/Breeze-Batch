package com.example.kybatch.domain.activity;

import com.example.kybatch.dto.DailyAggregationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

        /**
         * 하루 동안의 유저 활동(raw data)을 합산하여 DTO로 반환
         * - targetDate : 집계 기준 날짜 (예: 2025-11-27)
         * - startOfDay : 해당 날짜 00:00
         * - endOfDay   : 다음 날짜 00:00 (미만)
         */
        @Query("""
        SELECT new com.example.kybatch.dto.DailyAggregationDTO(
            ua.userId,
            SUM(ua.loginCount),
            SUM(ua.viewCount),
            SUM(ua.orderCount)
        )
        FROM UserActivity ua
        WHERE ua.createdAt >= :startOfDay
          AND ua.createdAt < :endOfDay
        GROUP BY ua.userId
        """)
    List<DailyAggregationDTO> aggregateDaily(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay")LocalDateTime endOfDay);


    @Query("SELECT MIN(a.createdAt) FROM UserActivity a")
    LocalDateTime findMinCreatedAt();

    @Query("SELECT MAX(a.createdAt) FROM UserActivity a")
    LocalDateTime findMaxCreatedAt();

}
