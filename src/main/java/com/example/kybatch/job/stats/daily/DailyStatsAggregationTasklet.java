package com.example.kybatch.job.stats.daily;

import com.example.kybatch.domain.lock.BatchLockPolicy;
import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
import com.example.kybatch.dto.DailyAggregationDTO;
import com.example.kybatch.lock.BatchLockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStatsAggregationTasklet implements Tasklet {

    private final DailyStatusRepository dailyRepository;
    private final BatchLockService lockService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext context) {

        // ===============================
        // 0) Daily 전용 Lock
        // ===============================
        boolean locked = lockService.acquireLock(
                "DAILY_STATS",
                BatchLockPolicy.EXCLUSIVE,
                "DAILY",
                "일간 통계 중복 실행 방지"
        );

        if (!locked) {
            log.warn("[DailyStats] Lock not acquired. Skip.");
            return RepeatStatus.FINISHED;
        }

        try {
            // ===============================
            // 1) 운영 기준 날짜 계산 (어제)
            // ===============================
            LocalDate targetDate = LocalDate.now().minusDays(1);

            LocalDateTime start = targetDate.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            log.info("[DailyStats] Aggregating for date={}", targetDate);

            // ===============================
            // 2) 기존 데이터 삭제 (Idempotent)
            // ===============================
            dailyRepository.deleteByDate(targetDate);

            // ===============================
            // 3) 집계 SQL 실행
            // ===============================
            List<DailyAggregationDTO> aggregates =
                    dailyRepository.aggregateDaily(start, end);

            log.info("[DailyStats] Aggregated rows={}", aggregates.size());

            // ===============================
            // 4) 저장
            // ===============================
            for (DailyAggregationDTO dto : aggregates) {
                DailyStatus status = DailyStatus.builder()
                        .userId(dto.getUserId())
                        .date(targetDate)
                        .loginCount(dto.getLoginCount())
                        .viewCount(dto.getViewCount())
                        .orderCount(dto.getOrderCount())
                        .build();

                dailyRepository.save(status);
            }

            return RepeatStatus.FINISHED;

        } finally {
            // ===============================
            // 5) Lock 해제
            // ===============================
            lockService.releaseLock("DAILY_STATS");
        }
    }
}
