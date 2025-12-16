package com.example.kybatch.job.daily;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.dto.DailyAggregationDTO;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;

public class DailyAggregationProcessor
        implements ItemProcessor<DailyAggregationDTO, DailyStatus> {

    private final LocalDate targetDate;

    public DailyAggregationProcessor(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    @Override
    public DailyStatus process(DailyAggregationDTO item) {

        return DailyStatus.builder()
                .userId(item.getUserId())
                .date(targetDate)               // ✅ 여기서 주입
                .loginCount(item.getLoginCount())
                .viewCount(item.getViewCount())
                .orderCount(item.getOrderCount())
                .build();
    }
}
