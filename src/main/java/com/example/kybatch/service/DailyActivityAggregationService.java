package com.example.kybatch.service;

import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.dto.DailyAggregationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyActivityAggregationService {

    private final UserActivityRepository userActivityRepository;


    /**
     * 1) targetDate를 기준으로 하루의 시작/끝 계산
     * 2) JPA Repository 호출하여 집계된 데이터를 가져옴
     */
    public List<DailyAggregationDTO> aggregateDaily(LocalDate targetDate){

        /* 하루의 시작 00:00 */
        LocalDateTime startOfDay = targetDate.atStartOfDay();

        /* 하루의 끝 다음날 00: 00 직전 */
        LocalDateTime endOfDay  = startOfDay.plusDays(1);

        return userActivityRepository.aggregateDaily(
                targetDate, startOfDay, endOfDay
        );

    }
}
