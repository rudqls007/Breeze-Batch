package com.example.kybatch.job.test;

import com.example.kybatch.job.listener.JobExecutionLoggingListener;
import com.example.kybatch.job.listener.StepExecutionLoggingListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class LogTestJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final JobExecutionLoggingListener jobLoggingListener;
    private final StepExecutionLoggingListener stepLoggingListener;

    @Bean
    public Job logTestJob() {
        return new JobBuilder("logTestJob", jobRepository)
                .listener(jobLoggingListener)   // ★ 스프링 빈 사용
                .start(successStep())
                .next(failStep())
                .build();
    }

    @Bean
    public Step successStep() {
        return new StepBuilder("successStep", jobRepository)
                .listener(stepLoggingListener)  // ★ 스프링 빈 사용
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("[TEST] 성공 Step 실행됨");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step failStep() {
        return new StepBuilder("failStep", jobRepository)
                .listener(stepLoggingListener)  // ★ 스프링 빈 사용
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("[TEST] 실패 Step 실행됨");
                    throw new RuntimeException("의도적으로 실패 발생!");
                }, transactionManager)
                .build();
    }
}
