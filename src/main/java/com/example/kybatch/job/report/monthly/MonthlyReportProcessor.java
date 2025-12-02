package com.example.kybatch.job.report.monthly;

import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.dto.report.MonthlyReportDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MonthlyReportProcessor implements ItemProcessor<MonthlyStatus, MonthlyReportDTO> {

    @Override
    public MonthlyReportDTO process(MonthlyStatus item) throws Exception {
        return MonthlyReportDTO.builder()
                .userId(item.getUserId())
                .yearValue(item.getYear())
                .monthValue(item.getMonth())
                .viewCount(item.getViewCount())
                .orderCount(item.getOrderCount())
                .loginCount(item.getLoginCount())
                .build();
    }
}
