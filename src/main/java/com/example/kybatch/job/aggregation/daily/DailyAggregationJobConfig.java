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
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Configuration          // ✅ 스프링 설정 클래스
@RequiredArgsConstructor // ✅ 생성자 주입 자동 생성 (lombok)
public class DailyAggregationJobConfig {

    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    private final JobExecutionLoggingListener jobExecutionLoggingListener;
    private final StepExecutionLoggingListener stepExecutionLoggingListener;


    /** ========================
     * Reader
     * ======================== */
    @Bean
    @StepScope
    public JpaPagingItemReader<DailyAggregationDTO> dailyAggregationReader(
            @Value("#{jobParameters['targetDate']}") String targetDate
    ) {

        // 문자열로 들어온 targetDate(jobParameter)를 LocalDate로 변환
        LocalDate target = (targetDate != null)
                ? LocalDate.parse(targetDate)
                : LocalDate.now(); // 또는 기본값

        Map<String, Object> params = new HashMap<>();
        params.put("targetDate", target);
        params.put("start", target.atStartOfDay());               // 00:00:00
        params.put("end", target.plusDays(1).atStartOfDay());     // 다음날 00:00:00

        // UserActivity를 날짜 범위로 필터링해서
        // userId 기준으로 합계 집계하여 DailyAggregationDTO로 조회
        return new JpaPagingItemReaderBuilder<DailyAggregationDTO>()
                .name("dailyAggregationReader")
                .entityManagerFactory(emf)
                .pageSize(1000) // 대량 데이터 대비, 1000건씩 페이징
                .queryString(
                        "SELECT new com.example.kybatch.dto.DailyAggregationDTO(" +
                                "ua.userId, :targetDate, " +
                                "SUM(ua.loginCount), SUM(ua.viewCount), SUM(ua.orderCount)) " +
                                "FROM UserActivity ua " +
                                "WHERE ua.createdAt >= :start " +
                                "AND ua.createdAt < :end " +
                                "GROUP BY ua.userId"
                )
                .parameterValues(params)
                .build();
    }

    /** ========================
     * Processor
     * ======================== */
    @Bean
    public ItemProcessor<DailyAggregationDTO, DailyStatus> dailyAggregationProcessor() {
        // DTO -> DailyStatus 엔티티 변환
        return dto -> DailyStatus.builder()
                .userId(dto.getUserId())
                .date(dto.getDate())
                .loginCount(dto.getLoginCount())
                .viewCount(dto.getViewCount())
                .orderCount(dto.getOrderCount())
                .build();
    }

    /** ========================
     * Writer
     * ======================== */
    @Bean
    public JpaItemWriter<DailyStatus> dailyAggregationWriter() {
        // JPA를 이용해서 DailyStatus를 일괄 저장하는 Writer
        return new JpaItemWriterBuilder<DailyStatus>()
                .entityManagerFactory(emf)
                .build();
    }

    /** ========================
     * Step
     * ======================== */
    @Bean
    public Step dailyAggregationStep(
            JpaPagingItemReader<DailyAggregationDTO> dailyAggregationReader,
            ItemProcessor<DailyAggregationDTO, DailyStatus> dailyAggregationProcessor,
            JpaItemWriter<DailyStatus> dailyAggregationWriter
    ) {
        // Reader -> Processor -> Writer 를 1000건 단위(chunk)로 처리
        return new StepBuilder("dailyAggregationStep", jobRepository)
                .<DailyAggregationDTO, DailyStatus>chunk(1000, tm)
                .reader(dailyAggregationReader)
                .processor(dailyAggregationProcessor)
                .writer(dailyAggregationWriter)
                .listener(stepExecutionLoggingListener)
                .build();
    }

    /** ========================
     * Job
     * ======================== */
    @Bean
    public Job dailyAggregationJob(Step dailyAggregationStep) {
        // 단일 스텝으로 구성된 일별 집계 Job
        return new JobBuilder("dailyAggregationJob", jobRepository)
                .listener(jobExecutionLoggingListener)
                .start(dailyAggregationStep)
                .build();
    }
}
