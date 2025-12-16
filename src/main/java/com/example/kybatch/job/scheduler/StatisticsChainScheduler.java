package com.example.kybatch.job.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cron.enabled", havingValue = "true")
public class StatisticsChainScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    /**
     * 통계 배치 체인 스케줄러
     *
     * ✔ 매일 새벽 02:00 실행
     * ✔ Daily → Weekly → Monthly 순서 보장
     * ✔ 앞 단계 실패 시 다음 단계 실행되지 않음
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runStatisticsChain() throws Exception {

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        log.info("[Scheduler] Statistics chain start - {}", today);

        // ==========================
        // 1️⃣ Daily 통계 (항상 실행)
        // ==========================
        runDaily(today, now);

        // ==========================
        // 2️⃣ Weekly 통계 (월요일만)
        // ==========================
        if (isWeeklyBoundary(today)) {
            runWeekly(today, now);
        }

        // ==========================
        // 3️⃣ Monthly 통계 (1일만)
        // ==========================
        if (isMonthlyBoundary(today)) {
            runMonthly(today, now);
        }

        log.info("[Scheduler] Statistics chain finished");
    }

    /**
     * Daily 통계 실행
     * - 어제 날짜 기준 집계
     */
    private void runDaily(LocalDate today, LocalDateTime now) throws Exception {

        Job job = jobRegistry.getJob("dailyStatsAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addLocalDate("targetDate", today.minusDays(1))
                .addLocalDateTime("runAt", now) // 재실행을 위한 유니크 파라미터
                .toJobParameters();

        jobLauncher.run(job, params);
    }

    /**
     * Weekly 통계 실행
     * - 지난 주 월~일 기준
     */
    private void runWeekly(LocalDate today, LocalDateTime now) throws Exception {

        LocalDate start = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate end = start.plusWeeks(1);

        Job job = jobRegistry.getJob("weeklyStatsAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addString("startDate", start.toString())
                .addString("endDate", end.toString())
                .addLocalDateTime("runAt", now)
                .toJobParameters();

        jobLauncher.run(job, params);
    }

    /**
     * Monthly 통계 실행
     * - 지난 달 기준
     */
    private void runMonthly(LocalDate today, LocalDateTime now) throws Exception {

        LocalDate start = today.minusMonths(1).withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);

        Job job = jobRegistry.getJob("monthlyStatsAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addString("startDate", start.toString())
                .addString("endDate", end.toString())
                .addLocalDateTime("runAt", now)
                .toJobParameters();

        jobLauncher.run(job, params);
    }

    /**
     * 주차 경계 판단 (월요일)
     */
    private boolean isWeeklyBoundary(LocalDate today) {
        return today.getDayOfWeek() == DayOfWeek.MONDAY;
    }

    /**
     * 월 경계 판단 (1일)
     */
    private boolean isMonthlyBoundary(LocalDate today) {
        return today.getDayOfMonth() == 1;
    }
}
