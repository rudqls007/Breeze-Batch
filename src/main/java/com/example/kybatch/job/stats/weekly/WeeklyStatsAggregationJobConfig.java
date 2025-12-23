package com.example.kybatch.job.stats.weekly;

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
public class WeeklyStatsAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final WeeklyStatsAggregationTasklet weeklyStatsAggregationTasklet;

    private final JobExecutionLoggingListener jobExecutionLoggingListener;

    // STEP 30 알림 Listener
    private final BatchFailureNotificationListener failureNotificationListener;

    private final StepExecutionLoggingListener stepExecutionLoggingListener;

    // ✅ STEP 34 자동 재실행 리스너
    private final BatchAutoRestartJobListener batchAutoRestartJobListener;

    @Bean
    public Job weeklyStatsAggregationJob() {
        return new JobBuilder("weeklyStatsAggregationJob", jobRepository)
                // Job 실행 로그 저장
                .listener(jobExecutionLoggingListener)

                // Job 실패 시 알림 발송
                .listener(failureNotificationListener)

                // ✅ (STEP 34) 실패 시 AFTER_COMMIT + ASYNC로 1회 자동 재실행 요청
                .listener(batchAutoRestartJobListener)

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
