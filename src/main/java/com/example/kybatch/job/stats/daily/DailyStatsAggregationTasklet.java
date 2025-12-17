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
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext context) {

        // ===============================
        // 0) Lock
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

//            // 🔥 실패 알림 테스트: 파라미터로 제어 (forceFail=Y일 때만 실패)
//            String forceFail = (String) context.getStepContext()
//                    .getJobParameters()
//                    .get("forceFail");
//
//            if ("Y".equalsIgnoreCase(forceFail)) {
//                throw new RuntimeException("🔥 DailyStatsAggregationJob 실패 알림 테스트");
//            }

            // ===============================
            // 1) 날짜 계산 (전날 기준)
            // ===============================
            LocalDate targetDate = LocalDate.now().minusDays(1);

            LocalDateTime start = targetDate.atStartOfDay();
            LocalDateTime end   = targetDate.plusDays(1).atStartOfDay();

            log.info("[DailyStats] Aggregating date={} ({} ~ {})",
                    targetDate, start, end.minusSeconds(1));

            // ===============================
            // 2) 기존 Daily 삭제
            // ===============================
            dailyRepository.deleteByDate(targetDate);

            // ===============================
            // 3) 집계 SQL
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
            // 5) Unlock
            // ===============================
            lockService.releaseLock("DAILY_STATS");
        }
    }
}
