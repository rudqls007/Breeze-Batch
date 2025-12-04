package com.example.kybatch.job.weekly;

import com.example.kybatch.service.WeeklyAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class WeeklyAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final WeeklyAggregationService service;


    @Bean
    public Job weeklyAggregationJob(){
        return new JobBuilder("weeklyAggregationJob", jobRepository)
                .start(weeklyAggregationStep())
                .build();
    }

    @Bean
    public Step weeklyAggregationStep() {
        return new StepBuilder("weeklyAggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    int year = 2025;        /* 후에 JobParameter로 받기 */
                    int week = 48;

                    service.aggregateWeekly(year, week);
                    return RepeatStatus.FINISHED;

                }, txManager)
                .build();
    }


}
