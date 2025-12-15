package com.example.kybatch.job.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyStatsScheduler {

    private final JobLauncher jobLauncher;
    private final Job monthlyStatsAggregationJob;

    // 매달 1일 새벽 00:10 실행
    @Scheduled(cron = "0 10 0 1 * ?")
    public void runMonthlyStats() throws Exception {

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.minusMonths(1).withDayOfMonth(1); // 이전달 1일
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);        // 이번달 1일

        JobParameters params = new JobParametersBuilder()
                .addString("startDate", startOfMonth.toString())
                .addString("endDate", startOfNextMonth.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        log.info("[Scheduler][Monthly] run {} ~ {}", startOfMonth, startOfNextMonth.minusDays(1));

        jobLauncher.run(monthlyStatsAggregationJob, params);
    }
}
