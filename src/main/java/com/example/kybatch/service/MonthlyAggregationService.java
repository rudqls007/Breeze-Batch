package com.example.kybatch.service;

import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.domain.stats.MonthlyStatusRepository;
import com.example.kybatch.dto.MonthlyAggregationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthlyAggregationService {

    private final MonthlyStatusRepository repo;

    @Transactional
    public void aggregateMonthly(int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate next = start.plusMonths(1);

        List<MonthlyAggregationDTO> result =
                repo.aggregateMonthly(year, month, start, next);

        for (MonthlyAggregationDTO dto : result) {
            repo.save(
                    MonthlyStatus.builder()
                            .userId(dto.getUserId())
                            .year(dto.getYear())
                            .month(dto.getMonth())
                            .loginCount(dto.getLoginCount())
                            .viewCount(dto.getViewCount())
                            .orderCount(dto.getOrderCount())
                            .build()
            );
        }
    }
}
