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

    @Scheduled(cron = "0 0 4 1 * *")
    public void runMonthlyStats() throws Exception{
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end   = start.plusMonths(1);

        JobParameters params = new JobParametersBuilder()
                .addString("startDate", start.toString())
                .addString("endDate", end.toString())
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();


        log.info("[Scheduler] Run Monthly Stats: {} ~ {}", start, end.minusDays(1));
        jobLauncher.run(monthlyStatsAggregationJob, params);
    }
}
