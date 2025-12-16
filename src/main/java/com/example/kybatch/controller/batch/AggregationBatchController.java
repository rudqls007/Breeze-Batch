package com.example.kybatch.controller.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class AggregationBatchController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    /**
     * 📌 1) Daily Aggregation 배치 수동 실행
     */
    @PostMapping("/daily-aggregation")
    public ResponseEntity<String> runDailyAggregation(
            @RequestParam(required = false) String targetDate
    ) throws Exception {

        LocalDate date = (targetDate == null || targetDate.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(targetDate);

        Job job = jobRegistry.getJob("dailyAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", date.toString())
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        log.info("[API] dailyAggregationJob 실행 시작 targetDate={}", date);

        JobExecution execution = jobLauncher.run(job, params);

        return ResponseEntity.ok(
                "dailyAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }

    /**
     * 📌 2) Weekly Aggregation 배치 수동 실행
     */
    @PostMapping("/weekly-aggregation")
    public ResponseEntity<String> runWeeklyAggregation(
            @RequestParam String weekStartDate
    ) throws Exception {

        Job job = jobRegistry.getJob("weeklyAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addString("weekStartDate", weekStartDate)
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        log.info("[API] weeklyAggregationJob 실행 시작 weekStartDate={}", weekStartDate);

        JobExecution execution = jobLauncher.run(job, params);

        return ResponseEntity.ok(
                "weeklyAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }

    /**
     * 📌 3) Monthly Aggregation 배치 수동 실행
     */
    @PostMapping("/monthly-aggregation")
    public ResponseEntity<String> runMonthlyAggregation(
            @RequestParam String targetMonth
    ) throws Exception {

        Job job = jobRegistry.getJob("monthlyAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addString("targetMonth", targetMonth)
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        log.info("[API] monthlyAggregationJob 실행 시작 targetMonth={}", targetMonth);

        JobExecution execution = jobLauncher.run(job, params);

        return ResponseEntity.ok(
                "monthlyAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }

    /**
     * 📌 4) Weekly Stats 배치 수동 실행
     */
    @PostMapping("/weekly-stats")
    public ResponseEntity<String> runWeeklyStats(
            @RequestParam String weekStartDate
    ) throws Exception {

        Job job = jobRegistry.getJob("weeklyStatsAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addString("weekStartDate", weekStartDate)
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        log.info("[API] weeklyStatsAggregationJob 실행 시작 weekStartDate={}", weekStartDate);

        JobExecution execution = jobLauncher.run(job, params);

        return ResponseEntity.ok(
                "weeklyStatsAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }

    /**
     * 📌 5) Monthly Stats 배치 수동 실행
     */
    @PostMapping("/monthly-stats")
    public ResponseEntity<String> runMonthlyStats(
            @RequestParam String targetMonth
    ) throws Exception {

        Job job = jobRegistry.getJob("monthlyStatsAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addString("targetMonth", targetMonth)
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        log.info("[API] monthlyStatsAggregationJob 실행 시작 targetMonth={}", targetMonth);

        JobExecution execution = jobLauncher.run(job, params);

        return ResponseEntity.ok(
                "monthlyStatsAggregationJob 실행 완료. executionId=" + execution.getId()
        );
    }
}
