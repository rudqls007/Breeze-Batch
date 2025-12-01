package com.example.kybatch.job.monthly;

import com.example.kybatch.service.MonthlyAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MonthlyAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MonthlyAggregationService service;

    @Bean
    public Job monthlyAggregationJob() {
        return new JobBuilder("monthlyAggregationJob", jobRepository)
                .start(monthlyAggregationStep())
                .build();
    }

    @Bean
    public Step monthlyAggregationStep() {
        return new StepBuilder("monthlyAggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    int year = 2025;
                    int month = 11;
                    service.aggregateMonthly(year, month);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
