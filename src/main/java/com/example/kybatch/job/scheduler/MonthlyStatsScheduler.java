package com.example.kybatch.job.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyStatsScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    // 매달 1일 새벽 00:10 실행
    @Scheduled(cron = "0 10 0 1 * ?")
    public void runMonthlyStats() throws Exception {

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.minusMonths(1).withDayOfMonth(1); // 이전달 1일
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);        // 이번달 1일

        Job job = jobRegistry.getJob("monthlyStatsAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addString("startDate", startOfMonth.toString())
                .addString("endDate", startOfNextMonth.toString())
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        log.info(
                "[Scheduler][MonthlyStats] run {} ~ {}",
                startOfMonth,
                startOfNextMonth.minusDays(1)
        );

        jobLauncher.run(job, params);
    }
}
