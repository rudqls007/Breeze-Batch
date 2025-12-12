package com.example.kybatch.domain.stats;

import com.example.kybatch.dto.WeeklyAggregationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WeeklyStatusRepository extends JpaRepository<WeeklyStatus, Long> {

    /**
     * 주간 집계 쿼리
     * - DailyStatus 기준으로 start~end 구간의 유저별 합계를 구한다.
     * - end 는 미포함 (>= start AND < end)
     */
    @Query("""
        SELECT new com.example.kybatch.dto.WeeklyAggregationDTO(
            ds.userId,
            SUM(ds.loginCount),
            SUM(ds.viewCount),
            SUM(ds.orderCount)
        )
        FROM DailyStatus ds
        WHERE ds.date >= :start
        AND   ds.date <  :end
        GROUP BY ds.userId
        """)
    List<WeeklyAggregationDTO> aggregateWeekly(
            @Param("start") LocalDate startOfWeek,
            @Param("end") LocalDate endOfWeek
    );

    /**
     * 해당 연도/주차의 기존 통계 삭제
     */
    void deleteByYearAndWeekOfYear(int year, int weekOfYear);
}
