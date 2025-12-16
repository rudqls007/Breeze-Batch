package com.example.kybatch.job.stats.daily;

import com.example.kybatch.job.listener.JobExecutionLoggingListener;
import com.example.kybatch.job.listener.StepExecutionLoggingListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

@Profile("batch")
@Configuration
@RequiredArgsConstructor
public class DailyStatsAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    private final DailyStatsAggregationTasklet tasklet;
    private final JobExecutionLoggingListener jobListener;
    private final StepExecutionLoggingListener stepListener;

    @Bean
    public Job dailyStatsAggregationJob() {
        return new JobBuilder("dailyStatsAggregationJob", jobRepository)
                .listener(jobListener)
                .start(dailyStatsAggregationStep())
                .build();
    }

    @Bean
    public Step dailyStatsAggregationStep() {
        return new StepBuilder("dailyStatsAggregationStep", jobRepository)
                .tasklet(tasklet, tm)
                .listener(stepListener)
                .build();
    }
}
