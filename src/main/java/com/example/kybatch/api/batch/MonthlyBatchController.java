package com.example.kybatch.api.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MonthlyBatchController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @PostMapping("/batch/monthly/run")
    public String runMonthlyBatch() throws Exception {

        Job job = jobRegistry.getJob("monthlyStatsAggregationJob");

        JobParameters params = new JobParametersBuilder()
                // 같은 달 재실행 가능하게 유니크 파라미터
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        jobLauncher.run(job, params);

        log.info("[API] Monthly batch triggered");
        return "Monthly batch started";
    }
}
