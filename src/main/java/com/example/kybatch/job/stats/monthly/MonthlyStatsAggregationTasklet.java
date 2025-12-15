package com.example.kybatch.job.stats.monthly;

import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.domain.stats.MonthlyStatusRepository;
import com.example.kybatch.dto.MonthlyAggregationDTO;
import com.example.kybatch.lock.BatchLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * MonthlyStatsAggregationTasklet
 * --------------------------------
 * - DailyStatus 기준으로 월간 통계를 생성하는 Tasklet.
 * - delete → aggregate → insert 패턴으로 동작.
 * - STEP 24에서 동시 실행 Lock + JobParameter 유효성 검증을 추가하여
 *   운영 환경에서도 안전하게 돌릴 수 있도록 고도화.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyStatsAggregationTasklet implements Tasklet {

    private final MonthlyStatusRepository monthlyRepository;

    /* STEP 24(2025-12-15) 추가 Batch Scheduler */
    private final BatchLockService lockService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext context) {

        /* STEP 24(2025-12-15) 동시 실행 방지 Lock 획득
        *  이미 같으 이름의 Lock이 존재하면 이번 실행은 바로 종료
        *  "MONTHLY_STATS" 라는 키로 월간 통계 잡을 보호함.
        * */
        if (!lockService.acquireLock("MONTHLY_STATS")) {
            log.warn("[MonthlyStats] Another instance is running. SKIP.");
            return RepeatStatus.FINISHED;
        }

        try {

            /* JobParameter 파싱
            *  - startDate : 해당 월의 1일 (예 : 2025-07-01)
            *  - endDate   : 다음 달의 1일 (예 : 2025-08-01, 미포함) */
            var params = context.getStepContext().getJobParameters();

            LocalDate startOfMonth = LocalDate.parse((String) params.get("startDate")); // 월 시작
            LocalDate startOfNextMonth = LocalDate.parse((String) params.get("endDate"));   // 다음달 시작(미포함)

            int year = startOfMonth.getYear();
            int month = startOfMonth.getMonthValue();


            /* STEP 24(2025-12-15) 날짜 유효성 검증
            *  - startOfMonth 는 반드시 "해당 월의 1일" 이어야 한다.
            *  - endDate 는 startDate + 1개월 이어야 한다.
            *  이 규칙이 깨지면 월간 배치가 아니라 다른 기간이므로 바로 예외처리
            * */
            if(startOfMonth.getDayOfMonth() != 1){
                throw new IllegalArgumentException(
                        "[MonthlyStats] startDate must be 1st day of the month. given=" + startOfMonth
                );
            }

            if (!startOfNextMonth.equals(startOfMonth.plusMonths(1))) {
                throw new IllegalArgumentException(
                        "[MonthlyStats] endDate must be startDate + 1 month. given=" + startOfNextMonth
                );
            }


            log.info("[MonthlyStats] Start aggregation: year={}, month={}, range {} ~ {}",
                    year, month, startOfMonth, startOfNextMonth.minusDays(1));

            /*  기존 월 데이터 삭제
            *   MonthlyStatus는 집계 결과 테이블이므로
            *   해당 연 / 월 데이터는 통째로 삭제 후 다시 생성하는 전략을 사용.
            *   재실행해도 항상 동일한 결과를 얻기 위한 idempotent 설계.*/
            monthlyRepository.deleteByYearAndMonth(year, month);

            /* 월간 데이터 집계
            *  DailyStatus 에서 startOfMonth ~ startOfNextMonth(미포함) 구간의
            *  유저별 login / view / order 합계를 구해옴.
            *  결과는 MonthlyAggregationDTO 리스트로 반환. */
            List<MonthlyAggregationDTO> aggregates =
                    monthlyRepository.aggregateMonthly(
                            year,
                            month,
                            startOfMonth,
                            startOfNextMonth
                    );

            log.info("[MonthlyStats] Aggregated user count = {}", aggregates.size());

            /* DTO -> MonthlyStatus 저장
            *  MonthlyAggregationDTO 에는 userId, year, month, login / view / order 합계가 들어있다.
            *  이를 MonthlyStatus 엔티티로 변환해서 저장한다. */
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

        } finally {

            /* STEP 24(2025-12-15) Lock 해제
            *  정상 종료든 예외든 상관없이 Lock은 반드시 해제되어야 한다.
            *  그래야 다음 실행에서 다시 acquireLock 이 가능해짐.*/
            lockService.releaseLock("MONTHLY_STATS");
        }
    }


}
