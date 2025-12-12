package com.example.kybatch.job.stats.monthly;

import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.domain.stats.MonthlyStatusRepository;
import com.example.kybatch.dto.MonthlyAggregationDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyStatsAggregationTasklet implements Tasklet {

    private final MonthlyStatusRepository monthlyRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext context) {

        var params = context.getStepContext().getJobParameters();

        LocalDate startOfMonth     = LocalDate.parse((String) params.get("startDate")); // 월 시작
        LocalDate startOfNextMonth = LocalDate.parse((String) params.get("endDate"));   // 다음달 시작(미포함)

        int year  = startOfMonth.getYear();
        int month = startOfMonth.getMonthValue();

        log.info("[MonthlyStats] Start aggregation: year={}, month={}, range {} ~ {}",
                year, month, startOfMonth, startOfNextMonth.minusDays(1));

        // 1) 기존 월 데이터 삭제
        monthlyRepository.deleteByYearAndMonth(year, month);

        // 2) SQL 한 방 집계 호출
        List<MonthlyAggregationDTO> aggregates =
                monthlyRepository.aggregateMonthly(
                        year,
                        month,
                        startOfMonth,
                        startOfNextMonth
                );

        log.info("[MonthlyStats] Aggregated user count = {}", aggregates.size());

        // 3) DTO -> MonthlyStatus 저장
        for (MonthlyAggregationDTO dto : aggregates) {

            MonthlyStatus status = MonthlyStatus.builder()
                    .userId(dto.getUserId())
                    .year(dto.getYear())
                    .month(dto.getMonth())
                    .loginCount(dto.getLoginCount())
                    .viewCount(dto.getViewCount())
                    .orderCount(dto.getOrderCount())
                    .build();

            monthlyRepository.save(status);
        }

        return RepeatStatus.FINISHED;
    }
}
