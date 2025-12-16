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
import java.time.temporal.TemporalAdjusters;

@Slf4j
@ConditionalOnProperty(name = "cron.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
public class WeeklyStatsScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Scheduled(cron = "0 0 3 * * MON")
    public void runWeeklyStats() throws Exception {

        LocalDate start = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusWeeks(1);

        Job job = jobRegistry.getJob("weeklyStatsAggregationJob");

        JobParameters params = new JobParametersBuilder()
                .addString("startDate", start.toString())
                .addString("endDate", end.toString())
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        log.info("[Scheduler][WeeklyStats] Run {} ~ {}", start, end.minusDays(1));

        jobLauncher.run(job, params);
    }
}
