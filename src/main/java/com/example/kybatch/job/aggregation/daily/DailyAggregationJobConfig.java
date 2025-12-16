package com.example.kybatch.job.aggregation.daily;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.dto.DailyAggregationDTO;
import com.example.kybatch.job.listener.JobExecutionLoggingListener;
import com.example.kybatch.job.listener.StepExecutionLoggingListener;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Profile("aggregation")
@Configuration
@RequiredArgsConstructor
public class DailyAggregationJobConfig {

    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    private final JobExecutionLoggingListener jobExecutionLoggingListener;
    private final StepExecutionLoggingListener stepExecutionLoggingListener;

    /* =========================================================
     * Reader
     * ========================================================= */
    @Bean
    @StepScope
    public JpaPagingItemReader<DailyAggregationDTO> dailyAggregationReader(
            @Value("#{jobParameters['targetDate']}") String targetDate
    ) {

        // jobParameter → LocalDate (없으면 어제 기준)
        LocalDate target = (targetDate != null)
                ? LocalDate.parse(targetDate)
                : LocalDate.now().minusDays(1);

        Map<String, Object> params = new HashMap<>();
        params.put("start", target.atStartOfDay());
        params.put("end", target.plusDays(1).atStartOfDay());

        return new JpaPagingItemReaderBuilder<DailyAggregationDTO>()
                .name("dailyAggregationReader")
                .entityManagerFactory(emf)
                .pageSize(1000)
                .queryString(
                        "SELECT new com.example.kybatch.dto.DailyAggregationDTO(" +
                                "ua.userId, " +
                                "SUM(ua.loginCount), " +
                                "SUM(ua.viewCount), " +
                                "SUM(ua.orderCount)) " +
                                "FROM UserActivity ua " +
                                "WHERE ua.createdAt >= :start " +
                                "AND ua.createdAt < :end " +
                                "GROUP BY ua.userId"
                )
                .parameterValues(params)
                .build();
    }

    /* =========================================================
     * Processor
     * ========================================================= */
    @Bean
    public ItemProcessor<DailyAggregationDTO, DailyStatus> dailyAggregationProcessor() {

        // ✅ date는 여기서만 관리
        LocalDate targetDate = LocalDate.now().minusDays(1);

        return dto -> DailyStatus.builder()
                .userId(dto.getUserId())
                .date(targetDate)
                .loginCount(dto.getLoginCount())
                .viewCount(dto.getViewCount())
                .orderCount(dto.getOrderCount())
                .build();
    }

    /* =========================================================
     * Writer
     * ========================================================= */
    @Bean
    public JpaItemWriter<DailyStatus> dailyAggregationWriter() {
        return new JpaItemWriterBuilder<DailyStatus>()
                .entityManagerFactory(emf)
                .build();
    }

    /* =========================================================
     * Step
     * ========================================================= */
    @Bean
    public Step dailyAggregationStep(
            JpaPagingItemReader<DailyAggregationDTO> dailyAggregationReader,
            ItemProcessor<DailyAggregationDTO, DailyStatus> dailyAggregationProcessor,
            JpaItemWriter<DailyStatus> dailyAggregationWriter
    ) {
        return new StepBuilder("dailyAggregationStep", jobRepository)
                .<DailyAggregationDTO, DailyStatus>chunk(1000, tm)
                .reader(dailyAggregationReader)
                .processor(dailyAggregationProcessor)
                .writer(dailyAggregationWriter)
                .listener(stepExecutionLoggingListener)
                .build();
    }

    /* =========================================================
     * Job
     * ========================================================= */
    @Bean
    public Job dailyAggregationJob(Step dailyAggregationStep) {
        return new JobBuilder("dailyAggregationJob", jobRepository)
                .listener(jobExecutionLoggingListener)
                .start(dailyAggregationStep)
                .build();
    }
}
