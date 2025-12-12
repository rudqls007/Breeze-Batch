package com.example.kybatch.job.aggregation.week;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.domain.stats.WeeklyStatusRepository;
import com.example.kybatch.job.listener.JobExecutionLoggingListener;
import com.example.kybatch.job.listener.StepExecutionLoggingListener;
import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WAGJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;
    private final EntityManagerFactory emf;
    private final WeeklyStatusRepository weeklyRepo;

    private final JobExecutionLoggingListener jobExecutionLoggingListener;
    private final StepExecutionLoggingListener stepExecutionLoggingListener;

    // ============================================================
    // 1) Job
    // ============================================================
    @Bean
    public Job weeklyAggregationJob(Step weeklyAggregationStep) {
        return new JobBuilder("weeklyAggregationJob", jobRepository)
                .listener(jobExecutionLoggingListener)
                .start(weeklyAggregationStep)
                .build();
    }

    // ============================================================
    // 2) Step
    // ============================================================
    @Bean
    public Step weeklyAggregationStep(
            JpaPagingItemReader<DailyStatus> weeklyDailyReader,
            WeeklyAggregationProcessor weeklyProcessor,
            WeeklyAggregationWriter weeklyWriter
    ) {
        return new StepBuilder("weeklyAggregationStep", jobRepository)
                .<DailyStatus, WeeklyStatus>chunk(1000, tm)
                .reader(weeklyDailyReader)
                .processor(weeklyProcessor)
                .writer(weeklyWriter)
                .listener(stepExecutionLoggingListener)   // ⭐ 변경된 부분
                .build();
    }

    // ============================================================
    // 3) Reader (해당 연도/주차 데이터를 DailyStatus에서 조회)
    // ============================================================
    @Bean
    @StepScope
    public JpaPagingItemReader<DailyStatus> weeklyDailyReader(
            @Value("#{jobParameters['year']}") Long yearParam,
            @Value("#{jobParameters['week']}") Long weekParam
    ) {

        int year = yearParam.intValue();
        int week = weekParam.intValue();

        // ----- ISO 기준 주차 → 해당 주의 월요일/일요일 구하기 -----
        WeekFields wf = WeekFields.ISO;
        LocalDate anchor = LocalDate.of(year, 1, 4);
        LocalDate monday = anchor
                .with(wf.weekOfYear(), week)
                .with(wf.dayOfWeek(), 1);
        LocalDate sunday = monday.plusDays(6);

        Map<String, Object> params = Map.of(
                "start", monday,
                "end", sunday
        );

        return new JpaPagingItemReaderBuilder<DailyStatus>()
                .name("weeklyDailyReader")
                .entityManagerFactory(emf)
                .queryString("SELECT d FROM DailyStatus d " +
                        "WHERE d.date BETWEEN :start AND :end")
                .parameterValues(params)
                .pageSize(1000)
                .build();
    }

    // ============================================================
    // 4) Processor (DailyStatus → WeeklyStatus 누적 집계)
    // ============================================================
    @Bean
    @StepScope
    public WeeklyAggregationProcessor weeklyProcessor(
            @Value("#{jobParameters['year']}") Long yearParam,
            @Value("#{jobParameters['week']}") Long weekParam
    ) {
        return new WeeklyAggregationProcessor(yearParam.intValue(), weekParam.intValue());
    }

    @Getter
    public static class WeeklyAggregationProcessor implements ItemProcessor<DailyStatus, WeeklyStatus> {

        private final int year;
        private final int week;
        private final Map<String, WeeklyStatus> map = new HashMap<>();

        public WeeklyAggregationProcessor(int year, int week) {
            this.year = year;
            this.week = week;
        }

        @Override
        public WeeklyStatus process(DailyStatus item) {

            Long userId = item.getUserId();
            String key = userId + "_" + year + "_" + week;

            WeeklyStatus weekly = map.get(key);

            if (weekly == null) {
                weekly = WeeklyStatus.builder()
                        .userId(userId)
                        .year(year)
                        .weekOfYear(week)
                        .loginCount(0)
                        .viewCount(0)
                        .orderCount(0)
                        .startDate(null)
                        .endDate(null)
                        .build();
                map.put(key, weekly);
            }

            // 누적
            weekly.setLoginCount(weekly.getLoginCount() + item.getLoginCount());
            weekly.setViewCount(weekly.getViewCount() + item.getViewCount());
            weekly.setOrderCount(weekly.getOrderCount() + item.getOrderCount());

            return null; // writer로 직접 전달하지 않음
        }

        public Collection<WeeklyStatus> aggregated() {
            return map.values();
        }
    }

    // ============================================================
    // 5) Writer (집계 결과 삭제 → 저장)
    // ============================================================
    @Bean
    @StepScope
    public WeeklyAggregationWriter weeklyWriter(
            WeeklyAggregationProcessor processor
    ) {
        return new WeeklyAggregationWriter(weeklyRepo, processor);
    }

    public static class WeeklyAggregationWriter implements ItemWriter<WeeklyStatus>, StepExecutionListener {

        private final WeeklyStatusRepository repo;
        private final WeeklyAggregationProcessor processor;

        private int year;
        private int week;

        public WeeklyAggregationWriter(WeeklyStatusRepository repo,
                                       WeeklyAggregationProcessor processor) {
            this.repo = repo;
            this.processor = processor;
        }

        @Override
        public void beforeStep(StepExecution stepExecution) {
            JobParameters params = stepExecution.getJobParameters();
            this.year = params.getLong("year").intValue();
            this.week = params.getLong("week").intValue();

            repo.deleteByYearAndWeekOfYear(year, week);
        }

        @Override
        public void write(Chunk<? extends WeeklyStatus> chunk) {
            // Processor는 null만 반환하므로 chunk는 비어 있음 → 무시
        }

        @Override
        public ExitStatus afterStep(StepExecution stepExecution) {
            repo.saveAll(processor.aggregated());
            return ExitStatus.COMPLETED;
        }
    }
}
