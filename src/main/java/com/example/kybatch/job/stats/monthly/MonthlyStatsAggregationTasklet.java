package com.example.kybatch.job.stats.monthly;

import com.example.kybatch.batch.exception.NonCriticalBatchException;
import com.example.kybatch.domain.lock.BatchLockPolicy;
import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.domain.stats.MonthlyStatusRepository;
import com.example.kybatch.dto.MonthlyAggregationDTO;
import com.example.kybatch.job.common.AbstractRetryableTasklet;
import com.example.kybatch.lock.BatchLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyStatsAggregationTasklet
        extends AbstractRetryableTasklet {

    private final MonthlyStatusRepository monthlyRepository;
    private final BatchLockService lockService;

    @Override
    protected void doExecute(StepContribution contribution,
                             ChunkContext context) {

        boolean locked = lockService.acquireLock(
                "MONTHLY_STATS",
                BatchLockPolicy.EXCLUSIVE,
                "MONTHLY",
                "월간 통계 중복 실행 방지"
        );

        if (!locked) {
            throw new NonCriticalBatchException("Monthly Lock 획득 실패");
        }

        try {
            LocalDate targetMonth = LocalDate.now().minusMonths(1);

            int year = targetMonth.getYear();
            int month = targetMonth.getMonthValue();

            // 1️⃣ 기존 월간 데이터 삭제 (idempotent 보장)
            monthlyRepository.deleteByYearAndMonth(year, month);

            // 2️⃣ 집계 기간 계산
            LocalDate startDate = targetMonth.withDayOfMonth(1);
            LocalDate startOfNextMonth = startDate.plusMonths(1);

            List<MonthlyAggregationDTO> aggregates =
                    monthlyRepository.aggregateMonthly(
                            year,
                            month,
                            startDate,
                            startOfNextMonth
                    );

            if (aggregates.isEmpty()) {
                throw new NonCriticalBatchException("Monthly 집계 결과 없음");
            }

            // 3️⃣ 저장
            for (MonthlyAggregationDTO dto : aggregates) {
                monthlyRepository.save(
                        MonthlyStatus.builder()
                                .userId(dto.getUserId())
                                .year(year)
                                .month(month)
                                .loginCount(dto.getLoginCount())
                                .viewCount(dto.getViewCount())
                                .orderCount(dto.getOrderCount())
                                .build()
                );
            }

        } finally {
            lockService.releaseLock("MONTHLY_STATS");
        }
    }
}
