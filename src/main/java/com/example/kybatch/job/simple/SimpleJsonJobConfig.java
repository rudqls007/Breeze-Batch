package com.example.kybatch.job.simple;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * STEP 2-2: JSON Writer Job 구성
 * -------------------------------------
 * [Job → Step → Tasklet 구조]
 * simpleJsonJob
 *   └─ simpleJsonStep
 *        └─ SimpleJsonTasklet
 *
 * - Spring Batch 5.0의 JobBuilder / StepBuilder 방식 사용
 */
@Configuration
@RequiredArgsConstructor
public class SimpleJsonJobConfig {

    private final SimpleJsonTasklet simpleJsonTasklet;

    @Bean
    public Job simpleJsonJob(JobRepository jobRepository, Step simpleJsonStep) {
        return new JobBuilder("simpleJsonJob", jobRepository)
                .start(simpleJsonStep)
                .build();
    }

    @Bean
    public Step simpleJsonStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("simpleJsonStep", jobRepository)
                .tasklet(simpleJsonTasklet, txManager)
                .build();
    }


}
