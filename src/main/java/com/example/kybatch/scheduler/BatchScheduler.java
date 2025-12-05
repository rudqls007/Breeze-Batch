package com.example.kybatch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "cron.enabled",   // 이 프로퍼티가
        havingValue = "true",    // true일 때만 Bean 등록 & 스케줄링 동작
        matchIfMissing = true    // (없으면 기본값 true로 보고 켜짐 - 운영에 유리)
)
public class BatchScheduler {

    private final JobLauncher jobLauncher;

    // --- Job Beans ---
    private final Job massiveUserActivityJob;
    private final Job dailyAggregationJob;
    private final Job weeklyAggregationJob;
    private final Job monthlyAggregationJob;

    // 1) 매일 00:00 → UserActivity 생성
    @Scheduled(cron = "0 5 0 * * *")
    public void runMassiveDaily() {
        LocalDate today = LocalDate.now();
        runJob(massiveUserActivityJob,
                new JobParametersBuilder()
                        .addString("targetDate", today.toString())
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );

        log.info("[CRON] MassiveUserActivityJob 실행 완료 - {}", today);
    }

    // 2) 매일 00:10 → Daily 집계
    @Scheduled(cron = "0 30 0 * * *")
    public void runDailyAggregation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        runJob(dailyAggregationJob,
                new JobParametersBuilder()
                        .addLong("year", (long) yesterday.getYear())
                        .addLong("month", (long) yesterday.getMonthValue())
                        .addLong("day", (long) yesterday.getDayOfMonth())
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );

        log.info("[CRON] DailyAggregationJob 실행 완료 - {}", yesterday);
    }

    // 3) 매주 월요일 00:50 → Weekly 집계
    @Scheduled(cron = "0 50 0 * * MON")
    public void runWeeklyAggregation() {
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);

        int year = lastWeek.getYear();
        int week = lastWeek.get(java.time.temporal.WeekFields.ISO.weekOfYear());

        runJob(weeklyAggregationJob,
                new JobParametersBuilder()
                        .addLong("year", (long) year)
                        .addLong("week", (long) week)
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );

        log.info("[CRON] WeeklyAggregationJob 실행 완료 - {}-W{}", year, week);
    }

    // 4) 매월 1일 01:00 → Monthly 집계
    @Scheduled(cron = "0 0 1 1 * *")
    public void runMonthlyAggregation() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);

        runJob(monthlyAggregationJob,
                new JobParametersBuilder()
                        .addLong("year", (long) lastMonth.getYear())
                        .addLong("month", (long) lastMonth.getMonthValue())
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );

        log.info("[CRON] MonthlyAggregationJob 실행 완료 - {}-{}",
                lastMonth.getYear(), lastMonth.getMonthValue());
    }

    // 공통 실행 함수
    private void runJob(Job job, JobParameters jobParameters) {
        try {
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            log.error("[CRON] Job 실행 실패: {}", job.getName(), e);
        }
    }
}
