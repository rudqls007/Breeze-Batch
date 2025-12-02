package com.example.kybatch.job.report.daily;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.dto.report.DailyReportDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 *  DailyStatus -> DailyReportDTO 변환
 * */
@Component
public class DailyReportProcessor implements ItemProcessor<DailyStatus, DailyReportDTO> {

    @Override
    public DailyReportDTO process(DailyStatus item)  {

        return DailyReportDTO.builder()
                .userId(item.getUserId())
                .date(item.getDate())
                .loginCount(item.getLoginCount())
                .viewCount(item.getViewCount())
                .orderCount(item.getOrderCount())
                .build();
    }
}
