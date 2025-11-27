package com.example.kybatch.job.daily;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.dto.DailyAggregationDTO;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;

/**
 * DailyAggregationProcessor
 * - DB 집계 결과(DTO)를 DailyStatus 엔티티로 변환하는 단계
 * - Processor는 "데이터 형태 변환"을 담당하는 Spring Batch 표준 구성 요소
 */
public class DailyAggregationProcessor implements ItemProcessor<DailyAggregationDTO, DailyStatus> {


    @Override
    public DailyStatus process(DailyAggregationDTO item)  {
        return DailyStatus.builder()
                .userId(item.getUserId())
                .date(item.getDate())
                .loginCount(item.getLoginCount())
                .viewCount(item.getViewCount())
                .orderCount(item.getOrderCount())
                .build();
    }
}
