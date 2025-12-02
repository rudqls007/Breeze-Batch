package com.example.kybatch.job.report.weekly;

import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.dto.report.WeeklyReportDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportProcessor implements ItemProcessor<WeeklyStatus, WeeklyReportDTO> {

    @Override
    public WeeklyReportDTO process(WeeklyStatus item) {
        return WeeklyReportDTO.builder()
                .userId(item.getUserId())
                .startDate(item.getStartDate())
                .endDate(item.getEndDate())
                .loginCount(item.getLoginCount())
                .viewCount(item.getViewCount())
                .orderCount(item.getOrderCount())
                .build();
    }
}
