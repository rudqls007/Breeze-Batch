package com.example.kybatch.service;

import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.domain.stats.WeeklyStatusRepository;
import com.example.kybatch.dto.WeeklyAggregationDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyAggregationService {

    private final WeeklyStatusRepository weeklyRepo;

    @Transactional
    public void aggregateWeekly(int year, int week){

        /* 컴파일 시점 날짜 포맷 */
        LocalDate startOfWeek = LocalDate
                .now()
                .withYear(year)
                .with(WeekFields.ISO.weekOfWeekBasedYear(), week)
                .with(WeekFields.ISO.dayOfWeek(), 1);

        /* 7일 후 -> 주차 집계 */
        LocalDate endOfWeek = startOfWeek.plusDays(7);

        List<WeeklyAggregationDTO> results =
                weeklyRepo.aggregateWeekly(startOfWeek, endOfWeek);

        for (WeeklyAggregationDTO dto : results) {
            weeklyRepo.save(
                    WeeklyStatus.builder()
                            .userId(dto.getUserId())
                            .year(year)
                            .weekOfYear(week)
                            .loginCount(dto.getLoginCount())
                            .viewCount(dto.getViewCount())
                            .orderCount(dto.getOrderCount())
                            .startDate(startOfWeek)
                            .endDate(endOfWeek.minusDays(1))
                            .build()
            );
        }
    }
}
