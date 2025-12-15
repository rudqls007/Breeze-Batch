package com.example.kybatch.controller.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class AggregationBatchController {

    private final JobLauncher jobLauncher;

    // 이미 @Bean으로 등록된 Job들을 주입받기
    private final Job dailyAggregationJob;
    private final Job weeklyAggregationJob;
    private final Job monthlyAggregationJob;
    private final Job weeklyStatsAggregationJob;
    private final Job monthlyStatsAggregationJob;

    /**
     * 📌 1) Daily Aggregation 배치 수동 실행
     * 예) POST /api/batch/daily-aggregation?targetDate=2025-12-15
     */
    @PostMapping("/daily-aggregation")
    public ResponseEntity<String> runDailyAggregation(
            @RequestParam(required = false) String targetDate
    ) throws Exception {

        // 파라미터 없으면 오늘 날짜로
        LocalDate date = (targetDate == null || targetDate.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(targetDate);

        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", date.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        log.info("[API] dailyAggregationJob 실행 시작 targetDate={}", date);

        JobExecution execution = jobLauncher.run(dailyAggregationJob, params);

        return ResponseEntity.ok(
                "dailyAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }

    /**
     * 📌 2) Weekly Aggregation 배치 수동 실행
     * 예) POST /api/batch/weekly-aggregation?weekStartDate=2025-12-08
     */
    @PostMapping("/weekly-aggregation")
    public ResponseEntity<String> runWeeklyAggregation(
            @RequestParam String weekStartDate
    ) throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("weekStartDate", weekStartDate)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        log.info("[API] weeklyAggregationJob 실행 시작 weekStartDate={}", weekStartDate);

        JobExecution execution = jobLauncher.run(weeklyAggregationJob, params);

        return ResponseEntity.ok(
                "weeklyAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }

    /**
     * 📌 3) Monthly Aggregation 배치 수동 실행
     * 예) POST /api/batch/monthly-aggregation?targetMonth=2025-12
     */
    @PostMapping("/monthly-aggregation")
    public ResponseEntity<String> runMonthlyAggregation(
            @RequestParam String targetMonth
    ) throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("targetMonth", targetMonth)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        log.info("[API] monthlyAggregationJob 실행 시작 targetMonth={}", targetMonth);

        JobExecution execution = jobLauncher.run(monthlyAggregationJob, params);

        return ResponseEntity.ok(
                "monthlyAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }

    /**
     * 📌 4) Weekly Stats 배치 수동 실행
     * 예) POST /api/batch/weekly-stats?weekStartDate=2025-12-08
     */
    @PostMapping("/weekly-stats")
    public ResponseEntity<String> runWeeklyStats(
            @RequestParam String weekStartDate
    ) throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("weekStartDate", weekStartDate)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        log.info("[API] weeklyStatsAggregationJob 실행 시작 weekStartDate={}", weekStartDate);

        JobExecution execution = jobLauncher.run(weeklyStatsAggregationJob, params);

        return ResponseEntity.ok(
                "weeklyStatsAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }

    /**
     * 📌 5) Monthly Stats 배치 수동 실행
     * 예) POST /api/batch/monthly-stats?targetMonth=2025-12
     */
    @PostMapping("/monthly-stats")
    public ResponseEntity<String> runMonthlyStats(
            @RequestParam String targetMonth
    ) throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("targetMonth", targetMonth)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        log.info("[API] monthlyStatsAggregationJob 실행 시작 targetMonth={}", targetMonth);

        JobExecution execution = jobLauncher.run(monthlyStatsAggregationJob, params);

        return ResponseEntity.ok(
                "monthlyStatsAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }
}
