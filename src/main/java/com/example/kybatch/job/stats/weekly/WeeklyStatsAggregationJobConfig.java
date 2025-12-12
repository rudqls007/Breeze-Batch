package com.example.kybatch.job.stats.weekly;

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
import org.springframework.transaction.PlatformTransactionManager;

/**
 * WeeklyStatsAggregationJobConfig
 * ------------------------------
 * - 주간 통계 집계 배치 Job 설정
 * - Tasklet 기반 Step 1개로 구성
 * - JobExecutionLoggingListener + StepExecutionLoggingListener 로 실행 로그 저장
 */
@Configuration
@RequiredArgsConstructor
public class WeeklyStatsAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final WeeklyStatsAggregationTasklet weeklyStatsAggregationTasklet;
    private final JobExecutionLoggingListener jobExecutionLoggingListener;
    private final StepExecutionLoggingListener stepExecutionLoggingListener;

    @Bean
    public Job weeklyStatsAggregationJob() {
        return new JobBuilder("weeklyStatsAggregationJob", jobRepository)
                .listener(jobExecutionLoggingListener)
                .start(weeklyStatsAggregationStep())
                .build();
    }

    @Bean
    public Step weeklyStatsAggregationStep() {
        return new StepBuilder("weeklyStatsAggregationStep", jobRepository)
                .tasklet(weeklyStatsAggregationTasklet, transactionManager)
                .listener(stepExecutionLoggingListener)
                .build();
    }
}
