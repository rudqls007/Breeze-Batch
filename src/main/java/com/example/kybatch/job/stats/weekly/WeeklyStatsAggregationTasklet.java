package com.example.kybatch.job.stats.weekly;

import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.domain.stats.WeeklyStatusRepository;
import com.example.kybatch.dto.WeeklyAggregationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;

/**
 * WeeklyStatsAggregationTasklet
 * ------------------------------
 * - DailyStatus를 기반으로 주간 집계를 수행하는 Tasklet.
 * - 1) JobParameter(startDate, endDate) 조회
 * - 2) 해당 기간에 대한 주차(year, weekOfYear) 계산
 * - 3) 기존 주차 데이터 삭제
 * - 4) aggregateWeekly 쿼리로 유저별 합계 조회
 * - 5) weekly_status 테이블에 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyStatsAggregationTasklet implements Tasklet {

    private final WeeklyStatusRepository weeklyStatusRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        var params = chunkContext.getStepContext().getJobParameters();

        String startDateStr = (String) params.get("startDate");
        String endDateStr   = (String) params.get("endDate");

        LocalDate startDate = LocalDate.parse(startDateStr); // 포함
        LocalDate endDate   = LocalDate.parse(endDateStr);   // 미포함 (다음 주 시작일)

        // ISO 주차 기준 연도/주차 계산
        int isoYear = startDate.get(IsoFields.WEEK_BASED_YEAR);
        int weekOfYear = startDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

        log.info("[WeeklyStats] startDate={}, endDate={}, isoYear={}, week={}",
                startDate, endDate, isoYear, weekOfYear);

        // 1) 기존 주차 데이터 삭제
        weeklyStatusRepository.deleteByYearAndWeekOfYear(isoYear, weekOfYear);

        // 2) 집계 쿼리 실행
        List<WeeklyAggregationDTO> aggregates =
                weeklyStatusRepository.aggregateWeekly(startDate, endDate);

        log.info("[WeeklyStats] aggregate result size = {}", aggregates.size());

        // 3) 결과를 WeeklyStatus 엔티티로 변환 후 저장
        for (WeeklyAggregationDTO dto : aggregates) {

            WeeklyStatus weeklyStatus = WeeklyStatus.builder()
                    .userId(dto.getUserId())
                    .year(isoYear)
                    .weekOfYear(weekOfYear)
                    .loginCount(dto.getLoginCount())
                    .viewCount(dto.getViewCount())
                    .orderCount(dto.getOrderCount())
                    .startDate(startDate)
                    .endDate(endDate.minusDays(1)) // end는 다음 주 시작일이므로 -1일
                    .build();

            weeklyStatusRepository.save(weeklyStatus);
        }

        return RepeatStatus.FINISHED;
    }
}
