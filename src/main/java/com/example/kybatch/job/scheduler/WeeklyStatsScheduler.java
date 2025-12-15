package com.example.kybatch.job.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyStatsScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklyStatsAggregationJob;

    @Scheduled(cron = "0 0 3 * * MON")
    public void runWeeklyStats() throws Exception {

        LocalDate start = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end   = start.plusWeeks(1);

        JobParameters params = new JobParametersBuilder()
                .addString("startDate", start.toString())
                .addString("endDate", end.toString())
                .addLong("runId", System.currentTimeMillis()) /* 중복 실행 방지용 */
                .toJobParameters();



        log.info("[Scheduler] Run Weekly Stats: {} ~ {}", start, end.minusDays(1));
        jobLauncher.run(weeklyStatsAggregationJob, params);


    }
}
