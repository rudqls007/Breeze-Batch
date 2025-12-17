package com.example.kybatch.domain.stats;

import com.example.kybatch.dto.MonthlyAggregationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MonthlyStatusRepository extends JpaRepository<MonthlyStatus, Long> {

    @Query("""
        SELECT new com.example.kybatch.dto.MonthlyAggregationDTO(
            d.userId,
            :year,
            :month,
            SUM(d.loginCount),
            SUM(d.viewCount),
            SUM(d.orderCount)
        )
        FROM DailyStatus d
        WHERE d.date >= :startOfMonth
          AND d.date < :startOfNextMonth
        GROUP BY d.userId
    """)
    List<MonthlyAggregationDTO> aggregateMonthly(
            @Param("year") int year,
            @Param("month") int month,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("startOfNextMonth") LocalDate startOfNextMonth);

    // ★ 여기 추가
    void deleteByYearAndMonth(int year, int month);

}