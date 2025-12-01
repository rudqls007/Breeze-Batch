package com.example.kybatch.service;

import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
import com.example.kybatch.dto.DailyAggregationDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyActivityAggregationService {

    private final UserActivityRepository userActivityRepository;
    private final DailyStatusRepository dailyStatusRepository;

    @Transactional
    public List<DailyAggregationDTO> aggregateDaily(LocalDate targetDate){

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay  = startOfDay.plusDays(1);

        List<DailyAggregationDTO> results =
                userActivityRepository.aggregateDaily(targetDate, startOfDay, endOfDay);

        for (DailyAggregationDTO dto : results) {
            DailyStatus status = DailyStatus.builder()
                    .userId(dto.getUserId())
                    .date(dto.getDate())
                    .loginCount(dto.getLoginCount())
                    .viewCount(dto.getViewCount())
                    .orderCount(dto.getOrderCount())
                    .build();

            dailyStatusRepository.save(status);
        }

        return results;   // ← ★ 추가 ★
    }

}