package com.example.kybatch.api.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MonthlyBatchController {

    private final JobLauncher jobLauncher;
    private final Job monthlyStatsAggregationJob;

    @PostMapping("/batch/monthly/run")
    public String runMonthlyBatch() throws Exception {

        JobParameters params = new JobParametersBuilder()
                // 같은 달 재실행 가능하게 timestamp만 추가
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(monthlyStatsAggregationJob, params);

        log.info("[API] Monthly batch triggered");
        return "Monthly batch started";
    }
}
