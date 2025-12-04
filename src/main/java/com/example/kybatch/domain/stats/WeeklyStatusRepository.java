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

    void deleteByYearAndWeekOfYear(int year, int weekOfYear);

}
