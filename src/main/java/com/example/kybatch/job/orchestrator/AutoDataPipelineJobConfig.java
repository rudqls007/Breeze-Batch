package com.example.kybatch.job.orchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AutoDataPipelineJobConfig {

    private final JobLauncher launcher;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    // 1) 이미 만들어 둔 Job들 주입
    private final Job massiveUserActivityJob;  // 더미 유저 액티비티 대량 생성
    private final Job dailyAggregationJob;     // 일간 통계 배치
    private final Job weeklyAggregationJob;    // 주간 통계 배치
    private final Job monthlyAggregationJob;   // 월간 통계 배치

    // ----------------------------------------------------
    // ⭐ 트랜잭션 끄기용 템플릿
    //    → TaskletStep 안은 트랜잭션이 걸려있으므로
    //      개별 Job 실행할 때는 기존 트랜잭션을 "잠시 중단"시키고 실행
    // ----------------------------------------------------
    @Bean
    public TransactionTemplate nonTxTemplate() {
        TransactionTemplate template = new TransactionTemplate(tm);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return template;
    }

    // ----------------------------------------------------
    // 2) 전체 파이프라인 Job
    //    - Step 하나(fullAutoPipelineStep) 안에서
    //      ① Massive → ② Daily → ③ Weekly → ④ Monthly 순서로 실행
    // ----------------------------------------------------
    @Bean
    public Job fullAutoPipelineJob(Step fullAutoPipelineStep) {
        return new JobBuilder("fullAutoPipelineJob", jobRepository)
                .start(fullAutoPipelineStep)
                .build();
    }

    // ----------------------------------------------------
    // 3) Pipeline Step (Tasklet)
    //    - 내부에서 다른 Job들을 순차 실행
    //    - Spring Batch 5.0 방식: tasklet(tasklet, transactionManager)
    // ----------------------------------------------------
    @Bean
    public Step fullAutoPipelineStep(TransactionTemplate nonTxTemplate) {

        return new StepBuilder("fullAutoPipelineStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    // -----------------------------
                    // (1) Massive Job 실행
                    // -----------------------------
                    runJobWithoutTx(
                            nonTxTemplate,
                            massiveUserActivityJob,
                            new JobParametersBuilder()
                                    .addLong("time", System.currentTimeMillis())
                                    .toJobParameters(),
                            "[AUTO] MassiveUserActivityJob"
                    );

                    // -----------------------------
                    // (2) Daily Aggregation (최근 120일)
                    //     start ~ end 날짜를 하루씩 돌면서
                    //     매일 DailyAggregationJob 실행
                    // -----------------------------
                    LocalDate start = LocalDate.now().minusDays(120);
                    LocalDate end = LocalDate.now();
                    LocalDate cursor = start;

                    while (!cursor.isAfter(end)) {
                        LocalDate target = cursor; // 람다 캡처용

                        JobParameters params = new JobParametersBuilder()
                                .addLong("year", (long) target.getYear())
                                .addLong("month", (long) target.getMonthValue())
                                .addLong("day", (long) target.getDayOfMonth())
                                .addLong("time", System.currentTimeMillis())
                                .toJobParameters();

                        runJobWithoutTx(
                                nonTxTemplate,
                                dailyAggregationJob,
                                params,
                                String.format("[AUTO] DailyAggregationJob (%s)", target)
                        );

                        cursor = cursor.plusDays(1);
                    }

                    // -----------------------------
                    // (3) Weekly Aggregation (최근 16주)
                    //     주 단위로 한 주씩 이동하면서
                    //     WeeklyAggregationJob 실행
                    // -----------------------------
                    LocalDate weekCursor = LocalDate.now().minusWeeks(16);

                    while (!weekCursor.isAfter(LocalDate.now())) {
                        int year = weekCursor.getYear();
                        int week = weekCursor.get(WeekFields.ISO.weekOfYear());

                        JobParameters params = new JobParametersBuilder()
                                .addLong("year", (long) year)
                                .addLong("week", (long) week)
                                .addLong("time", System.currentTimeMillis())
                                .toJobParameters();

                        runJobWithoutTx(
                                nonTxTemplate,
                                weeklyAggregationJob,
                                params,
                                String.format("[AUTO] WeeklyAggregationJob (%d년 %d주)", year, week)
                        );

                        weekCursor = weekCursor.plusWeeks(1);
                    }

                    // -----------------------------
                    // (4) Monthly Aggregation (최근 4개월)
                    //     월 단위로 움직이면서
                    //     MonthlyAggregationJob 실행
                    // -----------------------------
                    LocalDate monthCursor = LocalDate.now().minusMonths(4);

                    while (!monthCursor.isAfter(LocalDate.now())) {
                        int year = monthCursor.getYear();
                        int month = monthCursor.getMonthValue();

                        JobParameters params = new JobParametersBuilder()
                                .addLong("year", (long) year)
                                .addLong("month", (long) month)
                                .addLong("time", System.currentTimeMillis())
                                .toJobParameters();

                        runJobWithoutTx(
                                nonTxTemplate,
                                monthlyAggregationJob,
                                params,
                                String.format("[AUTO] MonthlyAggregationJob (%d년 %d월)", year, month)
                        );

                        monthCursor = monthCursor.plusMonths(1);
                    }

                    log.info("[AUTO] 전체 파이프라인 완료");

                    return RepeatStatus.FINISHED;
                }, tm)    // ⭐ 5.0: tasklet(tasklet, transactionManager)
                .build();
    }

    // ----------------------------------------------------
    // 🔧 공통 유틸: 트랜잭션 끄고 Job 한 번 실행
    //   - nonTxTemplate.execute() 안에서 launcher.run(...)
    //   - Spring Batch가 던지는 Checked Exception 4개 모두 처리
    // ----------------------------------------------------
    private void runJobWithoutTx(TransactionTemplate template,
                                 Job job,
                                 JobParameters params,
                                 String logPrefix) {

        template.execute(status -> {
            try {
                log.info("{} START", logPrefix);
                launcher.run(job, params);
                log.info("{} END", logPrefix);
            } catch (JobExecutionAlreadyRunningException |
                     JobRestartException |
                     JobInstanceAlreadyCompleteException |
                     JobParametersInvalidException e) {

                log.error("{} FAILED: {}", logPrefix, e.getMessage(), e);
                // 파이프라인 자체를 실패로 끝내고 싶으면 예외 재던지기
                throw new IllegalStateException("Failed to run job: " + logPrefix, e);
            }
            return null;
        });
    }
}
