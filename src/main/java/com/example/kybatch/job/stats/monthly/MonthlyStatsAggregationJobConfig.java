package com.example.kybatch.job.stats.monthly;

import com.example.kybatch.job.listener.BatchAutoRestartJobListener; // ✅ STEP 34 추가
import com.example.kybatch.job.listener.JobExecutionLoggingListener;
import com.example.kybatch.job.listener.StepExecutionLoggingListener;
import com.example.kybatch.notification.listener.BatchFailureNotificationListener;
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
public class MonthlyStatsAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    private final MonthlyStatsAggregationTasklet tasklet;

    private final JobExecutionLoggingListener jobListener;

    // STEP 30 알림 Listener
    private final BatchFailureNotificationListener failureNotificationListener;

    private final StepExecutionLoggingListener stepListener;

    // ✅ STEP 34 자동 재실행 리스너
    private final BatchAutoRestartJobListener batchAutoRestartJobListener;

    @Bean
    public Job monthlyStatsAggregationJob() {
        return new JobBuilder("monthlyStatsAggregationJob", jobRepository)
                // 실행 이력 기록
                .listener(jobListener)

                // 실패 알림 발송
                .listener(failureNotificationListener)

                // ✅ (STEP 34) 실패 시 AFTER_COMMIT + ASYNC로 1회 자동 재실행 요청
                .listener(batchAutoRestartJobListener)

                .start(monthlyStatsAggregationStep())
                .build();
    }

    @Bean
    public Step monthlyStatsAggregationStep() {
        return new StepBuilder("monthlyStatsAggregationStep", jobRepository)
                .tasklet(tasklet, tm)
                .listener(stepListener)
                .build();
    }
}
