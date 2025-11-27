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
         * 하루 동안의 유저 활동(raw data)을 합산하여 DTO 형태로 반환하는 JPQL
         * createdAt BETWEEN startOfDay AND endOfDay
         */
        @Query("""
        SELECT new com.example.kybatch.dto.DailyAggregationDTO(
            ua.userId,
            :targetDate,
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
            @Param("targetDate") LocalDate targetDate,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay")LocalDateTime endOfDay);

}
