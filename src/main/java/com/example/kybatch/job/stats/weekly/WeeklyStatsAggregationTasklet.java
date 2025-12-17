package com.example.kybatch.job.stats.weekly;

import com.example.kybatch.batch.exception.NonCriticalBatchException;
import com.example.kybatch.domain.lock.BatchLockPolicy;
import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.domain.stats.WeeklyStatusRepository;
import com.example.kybatch.dto.WeeklyAggregationDTO;
import com.example.kybatch.job.common.AbstractRetryableTasklet;
import com.example.kybatch.lock.BatchLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyStatsAggregationTasklet
        extends AbstractRetryableTasklet {

    private final WeeklyStatusRepository weeklyRepository;
    private final BatchLockService lockService;

    @Override
    protected void doExecute(StepContribution contribution,
                             ChunkContext context) {

        boolean locked = lockService.acquireLock(
                "WEEKLY_STATS",
                BatchLockPolicy.EXCLUSIVE,
                "WEEKLY",
                "주간 통계 중복 실행 방지"
        );

        if (!locked) {
            throw new NonCriticalBatchException("Weekly Lock 획득 실패");
        }

        try {
            LocalDate targetDate = LocalDate.now().minusDays(1);
            WeekFields wf = WeekFields.ISO;

            int year = targetDate.getYear();
            int weekOfYear = targetDate.get(wf.weekOfWeekBasedYear());

            weeklyRepository.deleteByYearAndWeekOfYear(year, weekOfYear);

            LocalDate startDate = targetDate
                    .with(wf.weekOfWeekBasedYear(), weekOfYear)
                    .with(wf.dayOfWeek(), 1);

            LocalDate endDate = startDate.plusDays(7);

            List<WeeklyAggregationDTO> aggregates =
                    weeklyRepository.aggregateWeekly(startDate, endDate);

            if (aggregates.isEmpty()) {
                throw new NonCriticalBatchException("Weekly 집계 결과 없음");
            }

            for (WeeklyAggregationDTO dto : aggregates) {
                weeklyRepository.save(
                        WeeklyStatus.builder()
                                .userId(dto.getUserId())
                                .year(year)
                                .weekOfYear(weekOfYear)
                                .loginCount(dto.getLoginCount())
                                .viewCount(dto.getViewCount())
                                .orderCount(dto.getOrderCount())
                                .build()
                );
            }

        } finally {
            lockService.releaseLock("WEEKLY_STATS");
        }
    }
}
