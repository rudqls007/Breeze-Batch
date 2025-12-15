package com.example.kybatch.job.stats.monthly;

import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.domain.stats.MonthlyStatusRepository;
import com.example.kybatch.dto.MonthlyAggregationDTO;
import com.example.kybatch.lock.BatchLockService;
import com.example.kybatch.domain.lock.BatchLockPolicy;

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
    private final BatchLockService lockService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext context) {

        // ===============================
        // 0) 락 획득
        // ===============================
        boolean locked = lockService.acquireLock(
                "MONTHLY_STATS",
                BatchLockPolicy.EXCLUSIVE,
                "MONTHLY",
                "월간 통계 중복 실행 방지"
        );

        if (!locked) {
            log.warn("[MonthlyStats] Lock not acquired. Skip.");
            return RepeatStatus.FINISHED;
        }

        try {

            // ===============================
            // 1) 날짜 자동 계산 — 운영 방식
            // ===============================
            LocalDate now = LocalDate.now();

            // 이번 달 기준, 지난 달
            LocalDate start = now.minusMonths(1).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1);

            int year = start.getYear();
            int month = start.getMonthValue();

            log.info("[MonthlyStats] Aggregating for {}-{} ({} ~ {})",
                    year, month, start, end.minusDays(1));

            // ===============================
            // 2) 기존 월 데이터 삭제
            // ===============================
            monthlyRepository.deleteByYearAndMonth(year, month);

            // ===============================
            // 3) 집계 SQL 실행
            // ===============================
            List<MonthlyAggregationDTO> aggregates =
                    monthlyRepository.aggregateMonthly(
                            year, month, start, end
                    );

            log.info("[MonthlyStats] Aggregated rows={}", aggregates.size());

            // ===============================
            // 4) 저장
            // ===============================
            for (MonthlyAggregationDTO dto : aggregates) {
                MonthlyStatus status = MonthlyStatus.builder()
                        .userId(dto.getUserId())
                        .year(year)
                        .month(month)
                        .loginCount(dto.getLoginCount())
                        .viewCount(dto.getViewCount())
                        .orderCount(dto.getOrderCount())
                        .build();

                monthlyRepository.save(status);
            }

            return RepeatStatus.FINISHED;

        } finally {
            // ===============================
            // 5) 락 해제
            // ===============================
            lockService.releaseLock("MONTHLY_STATS");
        }
    }
}
