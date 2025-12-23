package com.example.kybatch.job.stats.daily;

import com.example.kybatch.job.listener.BatchAutoRestartJobListener;
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
public class DailyStatsAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    private final DailyStatsAggregationTasklet tasklet;

    // 기존 실행 로그 저장 Listener (그대로 유지)
    private final JobExecutionLoggingListener jobListener;

    // STEP 30: 실패 시 알림 발송 Listener 추가
    private final BatchFailureNotificationListener failureNotificationListener;

    // STEP 34 : 실패 시 알림 발송 후 재실행 여부 판단 후 재실행
    private final BatchAutoRestartJobListener batchAutoRestartJobListener;

    private final StepExecutionLoggingListener stepListener;

    @Bean
    public Job dailyStatsAggregationJob() {
        return new JobBuilder("dailyStatsAggregationJob", jobRepository)

                // 1️⃣ Job 실행 결과를 DB에 기록
                .listener(jobListener)

                .listener(batchAutoRestartJobListener)

                // 2️⃣ JobExecution이 FAILED면 Slack/Mail/Kakao 알림
                //    - 성공 시엔 아무 것도 하지 않음
                .listener(failureNotificationListener)

                .start(dailyStatsAggregationStep())
                .build();
    }

    @Bean
    public Step dailyStatsAggregationStep() {
        return new StepBuilder("dailyStatsAggregationStep", jobRepository)
                .tasklet(tasklet, tm)

                // Step 단위 처리 로그 (기존 유지)
                .listener(stepListener)

                .build();
    }
}
